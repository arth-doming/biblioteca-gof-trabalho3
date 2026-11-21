import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

/** Trabalho 3 - resolve as pendencias P1..P7 do modelo base. */
public class XFix3 {
    static PrintStream log;
    static IModel root;
    static ProjectAccessor pa;
    static BasicModelEditor bme;

    public static void main(String[] a) throws Exception {
        log = new PrintStream(new FileOutputStream(a[2]), true, "UTF-8");
        pa = AstahAPI.getAstahAPI().getProjectAccessor();
        pa.open(a[0], true, false, false);
        root = pa.getProject();
        bme = pa.getModelEditorFactory().getBasicModelEditor();
        ITransactionManager tm = pa.getTransactionManager();
        tm.beginTransaction();
        try {
            run();
            tm.endTransaction();
        } catch (Throwable t) {
            tm.abortTransaction();
            t.printStackTrace(log);
            log.println("ABORTED");
            pa.close();
            System.exit(2);
        }
        pa.saveAs(a[1]);
        pa.close();
        log.println("SAVED " + a[1]);
    }

    // ------------------------------------------------------------ helpers
    static IClass cls(String name) {
        for (INamedElement e : root.getOwnedElements())
            if (e instanceof IClass c && name.equals(c.getName())) return c;
        throw new RuntimeException("class not found: " + name);
    }

    static IPackage pkg(String name) {
        for (INamedElement e : root.getOwnedElements())
            if (e instanceof IPackage p && name.equals(p.getName())) return p;
        throw new RuntimeException("package not found: " + name);
    }

    static IClass clsIn(String pkg, String name) {
        for (INamedElement c : pkg(pkg).getOwnedElements())
            if (c instanceof IClass k && name.equals(k.getName())) return k;
        throw new RuntimeException("class not found: " + pkg + "::" + name);
    }

    /** operacao pelo nome; se houver sobrecarga, desempata pelo nome do tipo do 1o parametro. */
    static IOperation op(IClass c, String name, String... firstParamType) {
        IOperation found = null;
        for (IOperation o : c.getOperations()) {
            if (!o.getName().equals(name)) continue;
            if (firstParamType.length > 0) {
                IParameter[] ps = o.getParameters();
                if (ps.length == 0 || !ps[0].getTypeExpression().equals(firstParamType[0])) continue;
            }
            if (found != null) throw new RuntimeException("ambiguous op " + c.getName() + "." + name);
            found = o;
        }
        if (found == null) throw new RuntimeException("op not found " + c.getName() + "." + name);
        return found;
    }

    static IDiagram diag(String name) {
        for (IDiagram d : allDiagrams()) if (d.getName().equals(name)) return d;
        throw new RuntimeException("diagram not found: " + name);
    }

    static List<IDiagram> allDiagrams() {
        List<IDiagram> all = new ArrayList<>();
        collect(root, all);
        return all;
    }

    static void collect(INamedElement e, List<IDiagram> ds) {
        ds.addAll(Arrays.asList(e.getDiagrams()));
        if (e instanceof IPackage p) for (INamedElement c : p.getOwnedElements()) if (c != null && !(c instanceof IDiagram)) collect(c, ds);
        if (e instanceof IClass c) for (IClass n : c.getNestedClasses()) collect(n, ds);
    }

    static IPresentation pres(IDiagram d, Object model) throws Exception {
        for (IPresentation p : d.getPresentations()) if (p.getModel() == model) return p;
        return null;
    }

    static IOperation addOp(IClass c, String name, Object ret, Object... params) throws Exception {
        IOperation o = ret instanceof IClass rc ? bme.createOperation(c, name, rc) : bme.createOperation(c, name, (String) ret);
        for (int i = 0; i < params.length; i += 2) {
            Object pt = params[i + 1];
            if (pt instanceof IClass pc) bme.createParameter(o, (String) params[i], pc); else bme.createParameter(o, (String) params[i], (String) pt);
        }
        log.println("  + op " + c.getName() + "." + name);
        return o;
    }

    static IAttribute addAttr(IClass c, String name, Object type) throws Exception {
        IAttribute at = type instanceof IClass tc ? bme.createAttribute(c, name, tc) : bme.createAttribute(c, name, (String) type);
        at.setVisibility("private");
        return at;
    }

    static IAssociation assoc(IClass a, String multA, IClass b, String multB, String roleB, String name, boolean aComposite) throws Exception {
        IAssociation as = bme.createAssociation(a, b, name, "", "");
        for (IAttribute end : as.getMemberEnds()) {
            if (end.getType() == b && !(a == b)) { end.setMultiplicityString(multB); if (!roleB.isEmpty()) end.setName(roleB); }
            else { end.setMultiplicityString(multA); if (aComposite) end.setComposite(); }
        }
        return as;
    }

    static void noteReplace(IDiagram d, String from, String to) throws Exception {
        for (IPresentation p : d.getPresentations())
            if ("Note".equals(p.getType()) && p.getLabel().contains(from)) {
                String text = p.getLabel().replace(from, to);
                try {
                    p.setLabel(text);
                } catch (Exception e) {
                    INodePresentation old = (INodePresentation) p;
                    ClassDiagramEditor ed = pa.getDiagramEditorFactory().getClassDiagramEditor();
                    ed.setDiagram(d);
                    INodePresentation n = ed.createNote(text, old.getLocation());
                    try { n.setWidth(old.getWidth()); n.setHeight(old.getHeight()); } catch (Exception ignored) { }
                    ed.deletePresentation(old);
                }
                log.println("  nota atualizada em " + d.getName());
                return;
            }
        throw new RuntimeException("texto de nota nao encontrado em " + d.getName() + ": " + from);
    }

    // ------------------------------------------------------------ construtor de sequencias
    static class Msg {
        ILinkPresentation lp;
        Msg(ILinkPresentation lp) { this.lp = lp; }
        INodePresentation act() { return (INodePresentation) lp.getTarget(); }
    }

    static class Frag {
        String type; int depth;
        double top, bottom;
        List<Double> dividers = new ArrayList<>();
        List<String> guards = new ArrayList<>();
        List<List<IPresentation>> ops = new ArrayList<>();
    }

    static class Seq {
        SequenceDiagramEditor e;
        ISequenceDiagram sd;
        double y = 110;
        Deque<Frag> open = new ArrayDeque<>();
        List<Frag> done = new ArrayList<>();
        double x = 50;

        Seq(String name) throws Exception {
            e = pa.getDiagramEditorFactory().getSequenceDiagramEditor();
            for (IDiagram d : allDiagrams())
                if (d instanceof ISequenceDiagram && d.getName().equals(name)) {
                    e.setDiagram(d);
                    e.deleteDiagram();
                    log.println("[seq] removido diagrama antigo " + name);
                }
            sd = e.createSequenceDiagram(root, name);
            log.println("[seq] criado " + name);
        }

        INodePresentation actor(String name, double gap) throws Exception {
            INodePresentation n = e.createLifeline(name, x);
            n.getModel().addStereotype("actor");
            x += gap;
            return n;
        }

        INodePresentation obj(IClass base, double gap) throws Exception {
            INodePresentation n = e.createLifeline("", x);
            ((ILifeline) n.getModel()).setBase(base);
            x += gap;
            return n;
        }

        void record(IPresentation p) {
            for (Frag f : open) f.ops.get(f.ops.size() - 1).add(p);
        }

        INodePresentation src(Object from) { return from instanceof Msg m ? m.act() : (INodePresentation) from; }

        Msg call(Object from, INodePresentation to, String name, String args, IOperation op) throws Exception {
            ILinkPresentation lp = e.createMessage(name, src(from), to, y);
            IMessage m = (IMessage) lp.getModel();
            if (op != null) m.setOperation(op);
            m.setName(name);
            m.setArgument(args);
            record(lp);
            y += 45;
            return new Msg(lp);
        }

        Msg self(Msg from, INodePresentation lifeline, String name, String args, IOperation op) throws Exception {
            ILinkPresentation lp;
            try { lp = e.createMessage(name, from.act(), from.act(), y); }
            catch (Exception ex) { lp = e.createMessage(name, from.act(), lifeline, y); }
            IMessage m = (IMessage) lp.getModel();
            if (op != null) m.setOperation(op);
            m.setName(name);
            m.setArgument(args);
            record(lp);
            y += 50;
            return new Msg(lp);
        }

        Msg create(Object from, INodePresentation to, String args) throws Exception {
            ILinkPresentation lp = e.createCreateMessage("criar", src(from), to, y);
            ((IMessage) lp.getModel()).setArgument(args);
            record(lp);
            y += 60;
            return new Msg(lp);
        }

        void ret(Msg m, String value) throws Exception {
            ILinkPresentation r = e.createReturnMessage(value, m.lp);
            record(r);
            y = Math.max(y, r.getPoints()[0].getY() + 40);
        }

        void begin(String type, String guard) {
            Frag f = new Frag();
            f.type = type; f.depth = open.size();
            f.guards.add(guard);
            f.ops.add(new ArrayList<>());
            open.push(f);
            f.top = y - 10;
            y += 60;
        }

        void next(String guard) {
            Frag f = open.peek();
            f.guards.add(guard);
            f.ops.add(new ArrayList<>());
            f.dividers.add(y - 10);
            y += 60;
        }

        void end() {
            Frag f = open.pop();
            // o fragmento termina logo abaixo da ultima mensagem que contem, para nao engolir o retorno externo
            double last = f.top;
            for (List<IPresentation> o : f.ops) for (IPresentation p : o) last = Math.max(last, ((ILinkPresentation) p).getPoints()[0].getY());
            f.bottom = last + 22;
            done.add(f);
            y += 25;
        }

        void finish() throws Exception {
            for (Frag f : done) {
                INodePresentation cf = null;
                if (f.depth == 0 && f.ops.size() > 1) {
                    // preferencia: fragmento criado a partir das mensagens (os operandos ficam donos delas)
                    cf = e.createCombinedFragment(f.type, f.ops.get(0).toArray(new IPresentation[0]));
                    try {
                        for (int i = 1; i < f.ops.size(); i++)
                            e.addInteractionOperand(cf, f.ops.get(i).toArray(new IPresentation[0]), "", f.guards.get(i));
                    } catch (Exception ex) {
                        log.println("  fallback retangulo (" + ex.getMessage() + ")");
                        e.deletePresentation(cf);
                        cf = null;
                    }
                }
                if (cf == null) {
                    // o operando seguinte usa uma lifeline (o ator) fora do primeiro operando, ou o
                    // fragmento e aninhado: retangulo explicito e divisorias nas posicoes registradas
                    double inset = 15 * f.depth;
                    double h = f.bottom - f.top;
                    cf = e.createCombinedFragment("", f.type, new Point2D.Double(30 + inset, f.top), x - 60 - 2 * inset, h);
                    for (int i = 1; i < f.ops.size(); i++)
                        e.addInteractionOperand(cf, f.ops.get(i).toArray(new IPresentation[0]), "", f.guards.get(i));
                    for (int i = 0; i < f.dividers.size(); i++) {
                        double end = i + 1 < f.dividers.size() ? f.dividers.get(i + 1) : f.bottom;
                        cf.setProperty("operand." + (i + 2) + ".length", String.valueOf(end - f.dividers.get(i)));
                    }
                    if (!f.dividers.isEmpty()) {
                        try { cf.setProperty("operand.1.length", String.valueOf(f.dividers.get(0) - f.top)); } catch (Exception ex) { log.println("   op1 len: " + ex); }
                        for (int i = 0; i < f.dividers.size(); i++) {
                            double end = i + 1 < f.dividers.size() ? f.dividers.get(i + 1) : f.bottom;
                            cf.setProperty("operand." + (i + 2) + ".length", String.valueOf(end - f.dividers.get(i)));
                        }
                    }
                    log.println("   props=" + cf.getProperties() + " h=" + cf.getHeight());
                }
                ICombinedFragment m = (ICombinedFragment) cf.getModel();
                m.setInteractionOperator(f.type);
                m.getInteractionOperands()[0].setGuard(f.guards.get(0));
                log.println("  fragmento " + f.type + " " + f.guards + " @" + cf.getLocation() + " h=" + cf.getHeight());
            }
            for (IPresentation p : sd.getPresentations())
                if (p instanceof ILinkPresentation lp && p.getModel() instanceof IMessage m)
                log.println("   M y=" + lp.getPoints()[0].getY() + " " + m.getIndex() + " " + m.getName() + "(" + m.getArgument() + ")" + (m.isReturnMessage() ? " RETURN" : "")
                        + (m.getOperation() == null ? "" : " op=" + m.getOperation().getOwner() + "." + m.getOperation().getName()));
        }
    }

    // ------------------------------------------------------------ correcoes
    static void run() throws Exception {
        IClass livro = cls("Livro"), pessoa = cls("Pessoa"), estudante = cls("Estudante"), atendente = cls("Atendente");
        IClass emprestimo = cls("Emprestimo"), devolucao = cls("Devolucao"), boleto = cls("Boleto");
        IClass acervo = cls("Acervo"), mov = cls("Movimentacao"), relac = cls("Relacionamento"), reserva = cls("Reserva");
        IClass tratEmp = cls("TratadorDeEmprestimo"), tratDev = cls("TratadorDeDevolucao"), tratAcervo = cls("TratadorDeAcervo");
        IClass tratPessoa = cls("TratadorDePessoa"), tratEst = cls("TratadorDeEstudante"), tratAte = cls("TratadorDeAtendente");
        IClass iMulta = cls("iMulta"), iQuit = cls("iQuitacao");
        IClass date = cls("date");

        // ==================================================== P1 Multa + TratadorDeMulta
        log.println("[P1] Multa e TratadorDeMulta");
        IClass multa = bme.createClass(root, "Multa");
        addAttr(multa, "valor", "double");
        addAttr(multa, "diasAtraso", "int");
        addAttr(multa, "politicaAplicada", "String");
        addAttr(multa, "dataEmissao", date);
        IAttribute q = addAttr(multa, "quitada", "boolean");
        q.setInitialValue("false");
        addOp(multa, "getValor", "double");
        addOp(multa, "isQuitada", "boolean");
        IOperation quitar = addOp(multa, "quitar", "void");
        quitar.setDefinition("quitada = true; uma segunda notificacao do banco nao altera nada (idempotente)");

        IAttribute numBoleto = addAttr(boleto, "numero", "String");
        log.println("  + attr Boleto.numero " + numBoleto.getName());

        // iMulta passa a devolver/consultar Multa
        op(iMulta, "consultarMulta").setReturnType(multa);
        op(iMulta, "consultarMulta").getParameters()[0].setName("matricula");

        IClass tratMulta = bme.createClass(root, "TratadorDeMulta");
        IOperation emitirMulta = addOp(tratMulta, "emitirMulta", boleto, "idLivro", "int", "matricula", "String", "diasAtraso", "int");
        addOp(tratMulta, "consultarMulta", multa, "matricula", "String");
        IOperation regQuit = addOp(tratMulta, "registrarQuitacao", "void", "numeroBoleto", "String", "dataPagamento", date);
        regQuit.setDefinition("localiza a Multa do boleto e chama quitar(); notificacao repetida nao gera segunda quitacao");
        IRealization rMulta = bme.createRealization(tratMulta, iMulta, "");
        IRealization rQuit = bme.createRealization(tratMulta, iQuit, "");
        IDependency dMultaMov = bme.createDependency(mov, tratMulta, "");
        IDependency dMultaRel = bme.createDependency(relac, tratMulta, "");
        IDependency dDevMulta = bme.createDependency(iMulta, tratDev, "");

        // Devolucao -gera-> Multa -cobrada por-> Boleto ; Estudante deve Multas
        IAssociation oldDevBoleto = null;
        for (IAttribute at : devolucao.getAttributes())
            if (at.getAssociation() != null && at.getType() == boleto) oldDevBoleto = at.getAssociation();
        if (oldDevBoleto != null) { bme.delete(oldDevBoleto); log.println("  - assoc Devolucao-Boleto (substituida por Devolucao-Multa-Boleto)"); }
        IAssociation aDevMulta = assoc(devolucao, "1", multa, "0..1", "multa", "gera", false);
        IAssociation aMultaBol = assoc(multa, "1", boleto, "0..1", "boleto", "cobrada por", false);
        IAssociation aEstMulta = assoc(estudante, "1", multa, "0..*", "multas", "deve", false);

        layoutMulta(multa, tratMulta, iMulta, iQuit, boleto, devolucao, estudante, mov, relac, tratDev,
                new IElement[]{rMulta, rQuit, dMultaMov, dMultaRel, dDevMulta, aDevMulta, aMultaBol, aEstMulta});

        IDiagram cmp = diag("T02-3 Componentes");
        noteReplace(cmp, "Financeiro     -  Boleto (emissão da multa e registro da quitação)",
                "Financeiro     -  Multa, Boleto, TratadorDeMulta (realiza iMulta e iQuitacao)");

        // ==================================================== P2..P6 sequencias
        IClass livroC = livro;
        buildManterPessoa(tratPessoa, relac, pessoa);
        buildManterPapel("Manter Estudante", tratEst, relac, estudante, "Estudante", "matricula", "estudante", "dadosDoEstudante");
        buildManterPapel("Manter Atendente", tratAte, relac, atendente, "Atendente", "idAtendente", "atendente", "dadosDoAtendente");
        buildManterLivro(tratAcervo, acervo, livroC);
        buildReservar(tratEmp, relac, acervo, livroC, mov, reserva);
        buildConsultarReserva(tratEmp, relac, mov);
        buildEmprestar(tratEmp, mov, acervo, livroC, emprestimo);
        buildRenovar(tratEmp, mov, emprestimo, livroC);
        buildDevolucao(tratDev, mov, livroC, devolucao);
        buildEmitirBoleto(tratDev, mov, tratMulta, multa, estudante, boleto, emitirMulta, pessoa);

        // ==================================================== P7 modelo de dominio
        fixDomain();
    }

    // ---------------------------------------------------------------- sequencias
    static void buildManterPessoa(IClass trat, IClass relac, IClass pessoa) throws Exception {
        Seq s = new Seq("Manter Pessoa");
        INodePresentation adm = s.actor("administrador", 330);
        INodePresentation t = s.obj(trat, 240);
        INodePresentation r = s.obj(relac, 230);
        INodePresentation p = s.obj(pessoa, 200);
        s.begin("alt", "inserir: pessoa nao cadastrada");
        Msg c = s.call(adm, t, "inserirPessoa", "nome, sobrenome, cpf", op(trat, "inserirPessoa"));
        Msg c1 = s.call(c, r, "consultarPessoa", "cpf", op(relac, "consultarPessoa"));
        s.ret(c1, "null");
        s.create(c, p, "nome, sobrenome, cpf");
        s.call(c, r, "adicionar", "pessoa", op(relac, "adicionar", "Pessoa"));
        s.ret(c, "dadosDaPessoa");
        s.next("consultar: pessoa cadastrada");
        c = s.call(adm, t, "consultarPessoa", "cpf", op(trat, "consultarPessoa"));
        c1 = s.call(c, r, "consultarPessoa", "cpf", op(relac, "consultarPessoa"));
        s.ret(c1, "pessoa");
        s.ret(c, "dadosDaPessoa");
        s.next("atualizar: pessoa cadastrada");
        c = s.call(adm, t, "atualizarPessoa", "cpf, nome, sobrenome", op(trat, "atualizarPessoa"));
        c1 = s.call(c, r, "consultarPessoa", "cpf", op(relac, "consultarPessoa"));
        s.ret(c1, "pessoa");
        s.call(c, p, "atualizar", "nome, sobrenome", null);
        s.ret(c, "confirmacao");
        s.next("excluir: pessoa cadastrada");
        c = s.call(adm, t, "excluirPessoa", "cpf", op(trat, "excluirPessoa"));
        c1 = s.call(c, r, "consultarPessoa", "cpf", op(relac, "consultarPessoa"));
        s.ret(c1, "pessoa");
        s.call(c, r, "remover", "pessoa", op(relac, "remover", "Pessoa"));
        s.ret(c, "confirmacao");
        s.end();
        s.finish();
    }

    static void buildManterPapel(String name, IClass trat, IClass relac, IClass papel, String P, String id, String var, String dados) throws Exception {
        Seq s = new Seq(name);
        INodePresentation adm = s.actor("administrador", 340);
        INodePresentation t = s.obj(trat, 250);
        INodePresentation r = s.obj(relac, 250);
        INodePresentation p = s.obj(papel, 200);
        String low = var;
        s.begin("alt", "inserir: pessoa cadastrada e " + low + " nao cadastrado");
        Msg c = s.call(adm, t, "inserir" + P, "cpf, " + id, op(trat, "inserir" + P));
        Msg c1 = s.call(c, r, "consultarPessoa", "cpf", op(relac, "consultarPessoa"));
        s.ret(c1, "pessoa");
        c1 = s.call(c, r, "consultar" + P, id, op(relac, "consultar" + P));
        s.ret(c1, "null");
        s.create(c, p, id + ", pessoa");
        s.call(c, r, "adicionar", var, op(relac, "adicionar", P));
        s.ret(c, dados);
        s.next("consultar: " + low + " cadastrado");
        c = s.call(adm, t, "consultar" + P, id, op(trat, "consultar" + P));
        c1 = s.call(c, r, "consultar" + P, id, op(relac, "consultar" + P));
        s.ret(c1, var);
        s.ret(c, dados);
        s.next("atualizar: pessoa e " + low + " cadastrados");
        c = s.call(adm, t, "atualizar" + P, id + ", cpf", op(trat, "atualizar" + P));
        c1 = s.call(c, r, "consultar" + P, id, op(relac, "consultar" + P));
        s.ret(c1, var);
        c1 = s.call(c, r, "consultarPessoa", "cpf", op(relac, "consultarPessoa"));
        s.ret(c1, "pessoa");
        s.call(c, p, "atualizar", id + ", pessoa", null);
        s.ret(c, "confirmacao");
        s.next("excluir: " + low + " cadastrado");
        c = s.call(adm, t, "excluir" + P, id, op(trat, "excluir" + P));
        c1 = s.call(c, r, "consultar" + P, id, op(relac, "consultar" + P));
        s.ret(c1, var);
        s.call(c, r, "remover", var, op(relac, "remover", P));
        s.ret(c, "confirmacao");
        s.end();
        s.finish();
    }

    static void buildManterLivro(IClass trat, IClass acervo, IClass livro) throws Exception {
        Seq s = new Seq("Manter Livro");
        INodePresentation adm = s.actor("administrador", 300);
        INodePresentation t = s.obj(trat, 260);
        INodePresentation a = s.obj(acervo, 230);
        INodePresentation l = s.obj(livro, 200);
        s.begin("alt", "inserir: dados do livro validos");
        Msg c = s.call(adm, t, "inserirLivro", "titulo", op(trat, "inserirLivro"));
        s.create(c, l, "titulo");
        s.call(c, l, "setEstado", "LIVRE", op(livro, "setEstado"));
        s.call(c, a, "adicionarLivro", "livro", op(acervo, "adicionarLivro"));
        s.ret(c, "dadosDoLivro");
        s.next("consultar: livro cadastrado");
        c = s.call(adm, t, "consultarLivro", "idLivro", op(trat, "consultarLivro"));
        Msg c1 = s.call(c, a, "consultarLivro", "idLivro", op(acervo, "consultarLivro"));
        s.ret(c1, "livro");
        s.ret(c, "dadosDoLivro");
        s.next("atualizar: livro cadastrado");
        c = s.call(adm, t, "atualizarLivro", "idLivro, titulo", op(trat, "atualizarLivro"));
        c1 = s.call(c, a, "consultarLivro", "idLivro", op(acervo, "consultarLivro"));
        s.ret(c1, "livro");
        s.call(c, l, "atualizar", "titulo", null);
        s.ret(c, "confirmacao");
        s.next("excluir: livro cadastrado e LIVRE");
        c = s.call(adm, t, "excluirLivro", "idLivro", op(trat, "excluirLivro"));
        c1 = s.call(c, a, "consultarLivro", "idLivro", op(acervo, "consultarLivro"));
        s.ret(c1, "livro");
        c1 = s.call(c, l, "getEstado", "", op(livro, "getEstado"));
        s.ret(c1, "LIVRE");
        s.call(c, a, "removerLivro", "idLivro", op(acervo, "removerLivro"));
        s.ret(c, "confirmacao");
        s.end();
        s.finish();
    }

    static void buildReservar(IClass trat, IClass relac, IClass acervo, IClass livro, IClass mov, IClass reserva) throws Exception {
        Seq s = new Seq("Reservar Livro");
        INodePresentation est = s.actor("estudante", 330);
        INodePresentation t = s.obj(trat, 240);
        INodePresentation r = s.obj(relac, 200);
        INodePresentation a = s.obj(acervo, 180);
        INodePresentation l = s.obj(livro, 200);
        INodePresentation m = s.obj(mov, 200);
        INodePresentation rs = s.obj(reserva, 150);
        Msg c = s.call(est, t, "solicitarReserva", "idLivro, matricula", op(trat, "solicitarReserva"));
        Msg c1 = s.call(c, r, "consultarEstudante", "matricula", op(relac, "consultarEstudante"));
        s.ret(c1, "estudante");
        c1 = s.call(c, a, "consultarLivro", "idLivro", op(acervo, "consultarLivro"));
        s.ret(c1, "livro");
        c1 = s.call(c, l, "getEstado", "", op(livro, "getEstado"));
        s.ret(c1, "estado");
        s.begin("alt", "estudante cadastrado e estado = LIVRE");
        s.create(c, rs, "livro, estudante, data");
        s.call(c, m, "adicionar", "reserva", op(mov, "adicionar"));
        s.call(c, l, "setEstado", "RESERVADO", op(livro, "setEstado"));
        s.next("else");
        s.call(c, est, "informarErro", "\"reserva nao permitida\"", null);
        s.end();
        s.ret(c, "dadosDaReserva");
        s.finish();
    }

    static void buildConsultarReserva(IClass trat, IClass relac, IClass mov) throws Exception {
        Seq s = new Seq("Consultar Reserva");
        INodePresentation est = s.actor("estudante", 330);
        INodePresentation t = s.obj(trat, 260);
        INodePresentation r = s.obj(relac, 230);
        INodePresentation m = s.obj(mov, 200);
        Msg c = s.call(est, t, "consultarReservas", "idLivro, matricula", op(trat, "consultarReservas"));
        Msg c1 = s.call(c, r, "consultarEstudante", "matricula", op(relac, "consultarEstudante"));
        s.ret(c1, "estudante");
        c1 = s.call(c, m, "consultar", "idLivro, matricula", op(mov, "consultar"));
        s.ret(c1, "reserva");
        s.begin("alt", "reserva encontrada");
        s.call(c, est, "exibirReserva", "reserva", null);
        s.next("else");
        s.call(c, est, "informarErro", "\"nenhuma reserva para este livro\"", null);
        s.end();
        s.ret(c, "reserva");
        s.finish();
    }

    static void buildEmprestar(IClass trat, IClass mov, IClass acervo, IClass livro, IClass emprestimo) throws Exception {
        Seq s = new Seq("Emprestar Livro");
        INodePresentation est = s.actor("estudante", 330);
        INodePresentation t = s.obj(trat, 240);
        INodePresentation m = s.obj(mov, 200);
        INodePresentation a = s.obj(acervo, 200);
        INodePresentation l = s.obj(livro, 200);
        INodePresentation e = s.obj(emprestimo, 150);
        Msg c = s.call(est, t, "efetivarEmprestimo", "idLivro, matricula", op(trat, "efetivarEmprestimo"));
        Msg c1 = s.call(c, m, "consultar", "idLivro, matricula", op(mov, "consultar"));
        s.ret(c1, "reserva");
        s.begin("alt", "reserva do mesmo estudante encontrada");
        c1 = s.call(c, a, "consultarLivro", "idLivro", op(acervo, "consultarLivro"));
        s.ret(c1, "livro");
        s.create(c, e, "livro, matricula, data");
        s.call(c, m, "adicionar", "emprestimo", op(mov, "adicionar"));
        s.call(c, m, "remover", "reserva", op(mov, "remover"));
        s.call(c, l, "setEstado", "EMPRESTADO", op(livro, "setEstado"));
        s.next("else");
        s.call(c, est, "informarErro", "\"nao ha reserva para este livro\"", null);
        s.end();
        s.ret(c, "dadosDoEmprestimo");
        s.finish();
    }

    static void buildRenovar(IClass trat, IClass mov, IClass emprestimo, IClass livro) throws Exception {
        Seq s = new Seq("Renovar Emprestimo de Livro");
        INodePresentation est = s.actor("estudante", 330);
        INodePresentation t = s.obj(trat, 240);
        INodePresentation m = s.obj(mov, 200);
        INodePresentation e = s.obj(emprestimo, 200);
        INodePresentation l = s.obj(livro, 200);
        Msg c = s.call(est, t, "renovarEmprestimo", "idLivro, matricula", op(trat, "renovarEmprestimo"));
        Msg c1 = s.call(c, m, "consultar", "idLivro, matricula", op(mov, "consultar"));
        s.ret(c1, "emprestimo");
        c1 = s.call(c, e, "getNumRenovacoes", "", op(emprestimo, "getNumRenovacoes"));
        s.ret(c1, "numRenovacoes");
        s.begin("alt", "numRenovacoes < 2");
        s.call(c, e, "estenderVencimento", "30", op(emprestimo, "estenderVencimento"));
        s.call(c, l, "setEstado", "RENOVADO", op(livro, "setEstado"));
        s.next("else");
        s.call(c, est, "informarErro", "\"limite de duas renovacoes atingido\"", null);
        s.end();
        s.ret(c, "dadosDaRenovacao");
        s.finish();
    }

    static void buildDevolucao(IClass trat, IClass mov, IClass livro, IClass devolucao) throws Exception {
        Seq s = new Seq("Registrar Devolucao de Livro");
        INodePresentation at = s.actor("atendente", 400);
        INodePresentation t = s.obj(trat, 240);
        INodePresentation m = s.obj(mov, 200);
        INodePresentation l = s.obj(livro, 200);
        INodePresentation d = s.obj(devolucao, 150);
        // P4: a mensagem leva os tres argumentos da operacao
        Msg c = s.call(at, t, "registrarDevolucao", "idLivro, matricula, idAtendente", op(trat, "registrarDevolucao"));
        Msg c1 = s.call(c, m, "consultar", "idLivro, matricula", op(mov, "consultar"));
        s.ret(c1, "emprestimo");
        s.begin("alt", "emprestimo encontrado");
        s.create(c, d, "livro, matricula, dataDevolucao");
        s.call(c, m, "adicionar", "devolucao", op(mov, "adicionar"));
        // P3: regra 9.2 - o emprestimo sai da Movimentacao
        s.call(c, m, "remover", "emprestimo", op(mov, "remover"));
        s.call(c, l, "setEstado", "DEVOLVIDO", op(livro, "setEstado"));
        s.begin("opt", "dataDevolucao > dataVencimento");
        s.self(c, t, "emitirBoletoDeMulta", "idLivro, matricula", op(trat, "emitirBoletoDeMulta"));
        s.end();
        s.next("else");
        s.call(c, at, "informarErro", "\"nao ha emprestimo registrado\"", null);
        s.end();
        s.ret(c, "comprovanteDeDevolucao");
        s.finish();
    }

    static void buildEmitirBoleto(IClass trat, IClass mov, IClass tratMulta, IClass multa, IClass estudante, IClass boleto,
                                  IOperation emitirMulta, IClass pessoa) throws Exception {
        Seq s = new Seq("Emitir Boleto de Multa");
        INodePresentation at = s.actor("atendente", 340);
        INodePresentation t = s.obj(trat, 240);
        INodePresentation m = s.obj(mov, 200);
        INodePresentation tm = s.obj(tratMulta, 220);
        INodePresentation mu = s.obj(multa, 170);
        INodePresentation e = s.obj(estudante, 170);
        INodePresentation b = s.obj(boleto, 150);
        Msg c = s.call(at, t, "emitirBoletoDeMulta", "idLivro, matricula", op(trat, "emitirBoletoDeMulta"));
        Msg c1 = s.call(c, m, "consultar", "idLivro, matricula", op(mov, "consultar"));
        s.ret(c1, "devolucao");
        s.begin("opt", "devolucao encontrada e dataDevolucao > dataVencimento");
        s.self(c, t, "calcularDiasAtraso", "devolucao", null);
        Msg c2 = s.call(c, tm, "emitirMulta", "idLivro, matricula, diasAtraso", emitirMulta);
        s.create(c2, mu, "diasAtraso, politicaVigente");
        Msg c3 = s.call(c2, e, "getEmail", "", op(pessoa, "getEmail"));
        s.ret(c3, "email");
        s.create(c2, b, "multa");
        s.call(c2, b, "enviarPorEmail", "email", op(boleto, "enviarPorEmail"));
        s.ret(c2, "boleto");
        s.end();
        s.ret(c, "boletoDeMulta");
        s.finish();
    }

    // ---------------------------------------------------------------- P1 layout
    static void layoutMulta(IClass multa, IClass tratMulta, IClass iMulta, IClass iQuit, IClass boleto, IClass devolucao,
                            IClass estudante, IClass mov, IClass relac, IClass tratDev, IElement[] links) throws Exception {
        log.println("[layout] Class Diagram0 (P1)");
        IDiagram cd = diag("Class Diagram0");
        ClassDiagramEditor cde = pa.getDiagramEditorFactory().getClassDiagramEditor();
        cde.setDiagram(cd);
        cde.createNodePresentation(multa, new Point2D.Double(1640, 110));
        cde.createNodePresentation(tratMulta, new Point2D.Double(1480, -600));
        cde.createNodePresentation(iMulta, new Point2D.Double(2010, -630));
        cde.createNodePresentation(iQuit, new Point2D.Double(2010, -530));
        cde.createNodePresentation(iMulta, new Point2D.Double(-500, 1250));
        Map<IElement, List<INodePresentation>> byModel = new HashMap<>();
        for (IPresentation p : cd.getPresentations())
            if (p instanceof INodePresentation n && p.getModel() instanceof IElement e) byModel.computeIfAbsent(e, k -> new ArrayList<>()).add(n);
        for (IElement l : links) {
            IElement s, t;
            if (l instanceof IAssociation as) { s = as.getMemberEnds()[0].getType(); t = as.getMemberEnds()[1].getType(); }
            else if (l instanceof IDependency dp) { s = dp.getClient(); t = dp.getSupplier(); }
            else { IRealization r = (IRealization) l; s = r.getClient(); t = r.getSupplier(); }
            List<INodePresentation> ts = byModel.get(t);
            INodePresentation tp = ts.get(ts.size() - 1);
            // a realizacao de iMulta usa o pirulito de cima; a dependencia de TratadorDeDevolucao usa o de baixo
            if (t == iMulta && !(l instanceof IDependency)) tp = ts.get(0);
            cde.createLinkPresentation(l, byModel.get(s).get(0), tp);
        }
        for (IPresentation p : cd.getPresentations())
            if ("Frame".equals(p.getType())) {
                INodePresentation f = (INodePresentation) p;
                f.setWidth(Math.max(f.getWidth(), 2200 + 940));
            }
    }

    // ---------------------------------------------------------------- P7 dominio
    static void fixDomain() throws Exception {
        log.println("[P7] modelo de dominio: GrupoDePessoas/GrupoDeAcademicos/GrupoDeColaboradores -> Relacionamento");
        IClass gp = clsIn("Modelo de Dominio", "GrupoDePessoas");
        IClass ga = clsIn("Modelo de Dominio", "GrupoDeAcademicos");
        IClass gc = clsIn("Modelo de Dominio", "GrupoDeColaboradores");
        IClass est = clsIn("Modelo de Dominio", "Estudante");
        IClass ate = clsIn("Modelo de Dominio", "Atendente");
        gp.setName("Relacionamento");
        IDiagram dd = diag("SistemaEmprestimo");
        ClassDiagramEditor cde = pa.getDiagramEditorFactory().getClassDiagramEditor();
        cde.setDiagram(dd);
        bme.delete(ga);
        bme.delete(gc);
        INodePresentation pRel = (INodePresentation) pres(dd, gp);
        pRel.setLocation(new Point2D.Double(900, 135));
        IAssociation aE = assoc(gp, "1", est, "1..*", "", "participa", true);
        IAssociation aA = assoc(gp, "1", ate, "1..*", "", "participa", true);
        cde.createLinkPresentation(aE, pRel, (INodePresentation) pres(dd, est));
        cde.createLinkPresentation(aA, pRel, (INodePresentation) pres(dd, ate));
        log.println("  Relacionamento @" + pRel.getLocation());
    }
}
