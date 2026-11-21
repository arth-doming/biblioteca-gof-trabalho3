"""Gera os diagramas UML (fontes PlantUML + PDF via Graphviz/SVG) a partir de modelos.py.

Uso:  python3 docs/uml/gerar_uml.py      (requer graphviz 'dot', 'rsvg-convert' e pypdf)
"""
import html
import os
import subprocess
import sys

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)
from modelos import CLASSES, ESTADOS, TRANSICOES, SEQUENCIAS  # noqa: E402

PUML = os.path.join(AQUI, "puml")
PDF = os.path.join(AQUI, "pdf")
os.makedirs(PUML, exist_ok=True)
os.makedirs(PDF, exist_ok=True)
FONTE = "Helvetica"
COR_PAPEL = "#1f4e79"


def e(s):
    return html.escape(s, quote=True)


# ------------------------------------------------------------------ PlantUML (fonte editável)
def puml_classes(nome, titulo, classes, rel):
    out = ["@startuml " + nome, "title " + titulo, "skinparam classAttributeIconSize 0", "hide empty members"]
    for c, (est, papel, attrs, mets) in classes.items():
        alias = "C" + str(abs(hash(c)) % 10**8)
        kind = {"interface": "interface", "abstract": "abstract class", "enumeration": "enum", "record": "class"}.get(est, "class")
        stereo = []
        if est == "record":
            stereo.append("record")
        if papel:
            stereo.append(papel)
        st = (" <<" + ", ".join(stereo) + ">>") if stereo else ""
        out.append(f'{kind} "{c}" as {alias}{st} {{')
        out += ["  " + a for a in attrs]
        out += ["  " + m + ("" if "(" in m else "()") if est != "enumeration" else "  " + m for m in mets]
        out.append("}")
    alias = {c: "C" + str(abs(hash(c)) % 10**8) for c in classes}
    seta = {"herda": "--|>", "realiza": "..|>", "usa": "..>", "assoc": "-->", "comp": "*--", "agg": "o--"}
    for a, b, t, r in rel:
        if t in ("comp", "agg"):
            out.append(f"{alias[a]} {seta[t]} {alias[b]}" + (f" : {r}" if r else ""))
        else:
            out.append(f"{alias[a]} {seta[t]} {alias[b]}" + (f" : {r}" if r else ""))
    out.append("@enduml")
    return "\n".join(out)


def puml_estados():
    out = ["@startuml R01_MaquinaDeEstados", "title T02-1 Estados do Livro — alinhada ao padrão State",
           "hide empty description"]
    for s, acao in ESTADOS.items():
        out.append(f"state {s}")
        if acao:
            out.append(f"{s} : {acao}")
    for a, b, rot, metodo in TRANSICOES:
        a2 = "[*]" if a == "[*]" else a
        b2 = "[*]" if b == "[final]" else b
        out.append(f"{a2} --> {b2} : {rot}\\n<i>{metodo}</i>")
    out.append("@enduml")
    return "\n".join(out)


def puml_sequencia(nome, titulo, parts, msgs):
    out = ["@startuml " + nome, "title " + titulo, "autonumber"]
    al = {p: f"P{i}" for i, p in enumerate(parts)}
    for p in parts:
        tipo = "actor" if p in ("Estudante", "Atendente", "Administrador") else "participant"
        out.append(f'{tipo} "{p}" as {al[p]}')
    for m in msgs:
        if m[0] == "note":
            out.append(f"note across: {m[1]}")
        elif m[0] == "ret":
            out.append(f"{al[m[1]]} --> {al[m[2]]} : {m[3]}")
        else:
            out.append(f"{al[m[0]]} -> {al[m[1]]} : {m[2]}")
    out.append("@enduml")
    return "\n".join(out)


# ------------------------------------------------------------------ Graphviz (classes e estados)
def dot_classes(titulo, classes, rel):
    L = ["digraph G {", f'graph [rankdir=BT, fontname="{FONTE}", fontsize=18, labelloc=t, label="{e(titulo)}", nodesep=0.5, ranksep=0.7, pad=0.3];',
         f'node [shape=plain, fontname="{FONTE}", fontsize=10];', f'edge [fontname="{FONTE}", fontsize=9];']
    ids = {}
    for i, (c, (est, papel, attrs, mets)) in enumerate(classes.items()):
        ids[c] = f"n{i}"
        cab = ""
        if est in ("interface", "enumeration", "record"):
            cab += f"«{est}»<br/>"
        if papel:
            cab += f'<font color="{COR_PAPEL}"><i>«{e(papel)}»</i></font><br/>'
        nomefmt = f"<i><b>{e(c)}</b></i>" if est in ("abstract", "interface") else f"<b>{e(c)}</b>"
        fundo = "#fff8dc" if not papel or papel in ("", "subsistema", "raiz de composição", "interface") else "#fff2b3"
        rows = [f'<tr><td bgcolor="{fundo}" cellpadding="4">{cab}{nomefmt}</td></tr>']
        if attrs or est != "enumeration":
            rows.append('<tr><td align="left" balign="left" cellpadding="3">' + ("<br/>".join(e(a) for a in attrs) or " ") + "</td></tr>")
        if est != "enumeration":
            rows.append('<tr><td align="left" balign="left" cellpadding="3">' + ("<br/>".join(e(m) for m in mets) or " ") + "</td></tr>")
        L.append(f'{ids[c]} [label=<<table border="1" cellborder="1" cellspacing="0" color="#555555">{"".join(rows)}</table>>];')
    for a, b, t, r in rel:
        lab = f', label="{e(r)}"' if r else ""
        estilo = {
            "herda": "arrowhead=onormal",
            "realiza": "arrowhead=onormal, style=dashed",
            "usa": "arrowhead=vee, style=dashed, color=\"#666666\"",
            "assoc": "arrowhead=vee",
            "comp": "dir=back, arrowtail=diamond, arrowhead=none",
            "agg": "dir=back, arrowtail=odiamond, arrowhead=none",
        }[t]
        if t in ("comp", "agg"):
            L.append(f"{ids[b]} -> {ids[a]} [{estilo}{lab}];")
        else:
            L.append(f"{ids[a]} -> {ids[b]} [{estilo}{lab}];")
    L.append("}")
    return "\n".join(L)


def dot_estados():
    L = ["digraph G {", f'graph [rankdir=LR, fontname="{FONTE}", fontsize=18, labelloc=t, label="R1 — T02-1 Estados do Livro (cada transição = método da classe do estado de origem)", nodesep=0.6, ranksep=1.0, pad=0.3];',
         f'node [fontname="{FONTE}", fontsize=11];', f'edge [fontname="{FONTE}", fontsize=9];',
         'inicio [shape=circle, style=filled, fillcolor=black, label="", width=0.25];',
         'fim [shape=doublecircle, style=filled, fillcolor=black, label="", width=0.2];']
    for s, acao in ESTADOS.items():
        txt = f"<b>{s}</b>" + (f'<br/><font point-size="9">{e(acao)}</font>' if acao else "")
        L.append(f'{s} [shape=box, style="rounded,filled", fillcolor="#fff8dc", label=<{txt}>];')
    for a, b, rot, met in TRANSICOES:
        a2 = "inicio" if a == "[*]" else a
        b2 = "fim" if b == "[final]" else b
        L.append(f'{a2} -> {b2} [label=<{e(rot)}<br/><font color="{COR_PAPEL}"><i>{e(met)}</i></font>>];')
    L.append("}")
    return "\n".join(L)


# ------------------------------------------------------------------ sequência (SVG próprio)
def svg_sequencia(titulo, parts, msgs):
    larg_col = 190
    margem = 40
    topo = 70
    passo = 34
    W = margem * 2 + larg_col * len(parts)
    x = {p: margem + larg_col * i + larg_col / 2 for i, p in enumerate(parts)}
    H = topo + 60 + passo * (len(msgs) + 1) + 30
    s = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" font-family="{FONTE}" font-size="11">',
         '<defs><marker id="a" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto"><path d="M0,0 L10,4 L0,8 z" fill="#222"/></marker>'
         '<marker id="r" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto"><path d="M0,0 L10,4 L0,8" fill="none" stroke="#222"/></marker></defs>',
         f'<rect width="{W}" height="{H}" fill="white"/>',
         f'<text x="{margem}" y="30" font-size="16" font-weight="bold">{e(titulo)}</text>']
    for p in parts:
        cx = x[p]
        s.append(f'<rect x="{cx - 85}" y="{topo - 22}" width="170" height="30" fill="#fff8dc" stroke="#555"/>')
        s.append(f'<text x="{cx}" y="{topo - 3}" text-anchor="middle" font-weight="bold" font-size="10.5">{e(p)}</text>')
        s.append(f'<line x1="{cx}" y1="{topo + 8}" x2="{cx}" y2="{H - 20}" stroke="#999" stroke-dasharray="5,4"/>')
    y = topo + 40
    n = 0
    for m in msgs:
        if m[0] == "note":
            s.append(f'<rect x="{margem}" y="{y - 14}" width="{W - 2 * margem}" height="22" fill="#fdecea" stroke="#d9a0a0"/>')
            s.append(f'<text x="{W / 2}" y="{y + 1}" text-anchor="middle" font-style="italic">{e(m[1])}</text>')
        else:
            ret = m[0] == "ret"
            a, b, txt = (m[1], m[2], m[3]) if ret else m
            n += 1
            rot = f"{n}: {txt}"
            if a == b:
                cx = x[a]
                s.append(f'<path d="M{cx},{y - 6} h35 v14 h-33" fill="none" stroke="#222" marker-end="url(#a)"/>')
                s.append(f'<text x="{cx + 40}" y="{y + 4}">{e(rot)}</text>')
            else:
                x1, x2 = x[a], x[b]
                estilo = 'stroke-dasharray="6,4" marker-end="url(#r)"' if ret else 'marker-end="url(#a)"'
                s.append(f'<line x1="{x1}" y1="{y}" x2="{x2}" y2="{y}" stroke="#222" {estilo}/>')
                tx = min(x1, x2) + 6
                s.append(f'<text x="{tx}" y="{y - 5}">{e(rot)}</text>')
        y += passo
    s.append("</svg>")
    return "\n".join(s)


def svg_para_pdf(svg_txt, destino):
    tmp = destino.replace(".pdf", ".svg")
    with open(tmp, "w", encoding="utf-8") as f:
        f.write(svg_txt)
    subprocess.run(["rsvg-convert", "-f", "pdf", "-o", destino, tmp], check=True)
    os.remove(tmp)


def dot_para_pdf(dot_txt, destino):
    svg = subprocess.run(["dot", "-Tsvg"], input=dot_txt.encode("utf-8"), capture_output=True, check=True).stdout
    svg_para_pdf(svg.decode("utf-8"), destino)


def main():
    gerados = []
    # máquina de estados primeiro
    open(os.path.join(PUML, "R01_MaquinaDeEstados.puml"), "w", encoding="utf-8").write(puml_estados())
    for nome, (titulo, classes, rel) in CLASSES.items():
        open(os.path.join(PUML, nome + ".puml"), "w", encoding="utf-8").write(puml_classes(nome, titulo, classes, rel))
        destino = os.path.join(PDF, nome + ".pdf")
        dot_para_pdf(dot_classes(titulo, classes, rel), destino)
        gerados.append(destino)
        if nome == "R01_State":
            d2 = os.path.join(PDF, "R01_MaquinaDeEstados.pdf")
            dot_para_pdf(dot_estados(), d2)
            gerados.append(d2)
    for nome, (titulo, parts, msgs) in SEQUENCIAS.items():
        open(os.path.join(PUML, nome + ".puml"), "w", encoding="utf-8").write(puml_sequencia(nome, titulo, parts, msgs))
        destino = os.path.join(PDF, nome + ".pdf")
        svg_para_pdf(svg_sequencia(titulo, parts, msgs), destino)
        gerados.append(destino)

    from pypdf import PdfWriter
    w = PdfWriter()
    for g in gerados:
        w.append(g)
    final = os.path.join(AQUI, "Modelos_UML_Trabalho3.pdf")
    with open(final, "wb") as f:
        w.write(f)
    print(f"{len(gerados)} diagramas -> {final}")


if __name__ == "__main__":
    main()
