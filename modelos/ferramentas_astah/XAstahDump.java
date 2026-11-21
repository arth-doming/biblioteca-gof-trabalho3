import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import java.util.*;

public class XAstahDump {
    static String st(IElement e) { return e.getStereotypes().length > 0 ? " <<" + String.join(",", e.getStereotypes()) + ">>" : ""; }

    public static void main(String[] a) throws Exception {
        ProjectAccessor pa = AstahAPI.getAstahAPI().getProjectAccessor();
        pa.open(a[0], true, false, true);
        IModel root = pa.getProject();
        walk(root, "");
        System.out.println("\n===== DIAGRAMS =====");
        for (IDiagram d : root.getDiagrams()) diag(d);
        diagsIn(root);
        pa.close();
    }

    static void diagsIn(INamedElement e) throws Exception {
        if (e instanceof IPackage p) {
            for (INamedElement c : p.getOwnedElements()) {
                if (c != null) {
                    for (IDiagram d : c.getDiagrams()) diag(d);
                    diagsIn(c);
                }
            }
        }
        if (e instanceof IClass c) for (IClass n : c.getNestedClasses()) { for (IDiagram d : n.getDiagrams()) diag(d); }
    }

    static void diag(IDiagram d) throws Exception {
        System.out.println("\n## " + d.getClass().getSimpleName().replace("Imp", "") + " : " + d.getName() + "  [owner=" + ((INamedElement) d.getOwner()).getName() + "]");
        for (IPresentation p : d.getPresentations()) {
            Object m = p.getModel();
            String mn = m instanceof INamedElement ne ? ne.getName() : (m == null ? "-" : m.getClass().getSimpleName());
            String mt = m == null ? "" : m.getClass().getSimpleName().replace("Imp", "");
            System.out.println("   P " + p.getType() + " | " + mt + " | " + mn + " | label=" + p.getLabel());
        }
        if (d instanceof ISequenceDiagram sd) {
            IInteraction in = sd.getInteraction();
            for (ILifeline l : in.getLifelines()) System.out.println("   L " + l.getName() + " : " + (l.getBase() == null ? "-" : l.getBase().getName()));
            for (IMessage msg : in.getMessages()) {
                System.out.println("   M " + msg.getIndex() + " " + msg.getName() + "(" + msg.getArgument() + ") ret=" + msg.getReturnValue()
                        + " guard=" + msg.getGuard() + " op=" + (msg.getOperation() == null ? "-" : msg.getOperation().getOwner() + "." + msg.getOperation().getName())
                        + " src=" + nm(msg.getSource()) + " tgt=" + nm(msg.getTarget()) + (msg.isReturnMessage() ? " RETURN" : "") + (msg.isCreateMessage() ? " CREATE" : ""));
            }
        }
        if (d instanceof IStateMachineDiagram smd) {
            IStateMachine sm = smd.getStateMachine();
            for (IVertex v : sm.getVertexes()) {
                String extra = v instanceof IState s ? " entry=" + s.getEntry() + " do=" + s.getDoActivity() + " exit=" + s.getExit() : "";
                System.out.println("   V " + v.getClass().getSimpleName() + " " + v.getName() + extra);
            }
            for (ITransition t : sm.getTransitions())
                System.out.println("   T " + t.getSource().getName() + " -> " + t.getTarget().getName() + " : ev=" + t.getEvent() + " g=" + t.getGuard() + " a=" + t.getAction());
        }
    }

    static String nm(Object o) { return o instanceof INamedElement n ? n.getName() : String.valueOf(o); }

    static void walk(INamedElement e, String ind) {
        if (e instanceof IClass c) {
            System.out.println(ind + "CLASS " + c.getName() + st(c) + (c.isAbstract() ? " {abstract}" : "") + " id=" + c.getId());
            for (IGeneralization g : c.getGeneralizations()) System.out.println(ind + "  extends " + g.getSuperType().getName());
            for (IRealization r : c.getClientRealizations()) System.out.println(ind + "  realizes " + r.getSupplier().getName());
            for (IDependency r : c.getClientDependencies()) System.out.println(ind + "  depends " + r.getSupplier().getName() + st(r) + " '" + r.getName() + "'");
            for (IAttribute at : c.getAttributes()) {
                IAssociation as = at.getAssociation();
                if (as == null) System.out.println(ind + "  attr " + (at.isPublicVisibility()?"+":at.isPrivateVisibility()?"-":"#") + " " + at.getName() + " : " + at.getTypeExpression());
                else System.out.println(ind + "  end  " + at.getName() + " -> " + at.getType().getName() + " mult=" + mult(at) + " agg=" + (at.isComposite() ? "comp" : at.isAggregate() ? "agg" : "-") + " assoc='" + as.getName() + "'");
            }
            for (IOperation op : c.getOperations()) {
                StringBuilder sb = new StringBuilder();
                for (IParameter p : op.getParameters()) sb.append(p.getName()).append(":").append(p.getTypeExpression()).append(", ");
                System.out.println(ind + "  op " + (op.isPublicVisibility()?"+":op.isPrivateVisibility()?"-":"#") + " " + op.getName() + "(" + sb + ") : " + op.getReturnTypeExpression() + " id=" + op.getId());
            }
        } else {
            System.out.println(ind + e.getClass().getSimpleName().replace("Imp", "") + " " + e.getName() + st(e));
        }
        if (e instanceof IPackage p) for (INamedElement c : p.getOwnedElements()) if (c != null) walk(c, ind + "  ");
    }

    static String mult(IAttribute at) {
        StringBuilder sb = new StringBuilder();
        for (IMultiplicityRange r : at.getMultiplicity()) sb.append(r.getLower()).append("..").append(r.getUpper()).append(" ");
        return sb.toString().trim();
    }
}
