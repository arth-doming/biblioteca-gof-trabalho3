import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import java.io.*;
import java.util.*;
public class XProbe {
  public static void main(String[] a) throws Exception {
    PrintStream out = new PrintStream(new FileOutputStream(a[1]), true, "UTF-8");
    ProjectAccessor pa = AstahAPI.getAstahAPI().getProjectAccessor();
    pa.open(a[0], true, false, true);
    for (IDiagram d : pa.getProject().getDiagrams()) {
      if (!(d instanceof ISequenceDiagram)) continue;
      out.println("\n## " + d.getName());
      for (IPresentation p : d.getPresentations()) {
        Object m = p.getModel();
        String pos = "";
        if (p instanceof INodePresentation n) pos = String.format(" @(%.0f,%.0f %.0fx%.0f)", n.getLocation().getX(), n.getLocation().getY(), n.getWidth(), n.getHeight());
        if (p instanceof ILinkPresentation l) pos = String.format(" y=%.0f x0=%.0f x1=%.0f", l.getPoints()[0].getY(), l.getPoints()[0].getX(), l.getPoints()[l.getPoints().length-1].getX());
        if ("Activation".equals(p.getType())) continue;
        out.println("  " + p.getType() + pos + " | " + p.getLabel());
        if (m instanceof ICombinedFragment cf) {
          out.println("     props=" + p.getProperties());
          for (IInteractionOperand io : cf.getInteractionOperands()) {
            StringBuilder sb = new StringBuilder();
            for (IMessage mm : io.getMessages()) sb.append(mm.getName()).append(", ");
            out.println("     operand guard=[" + io.getGuard() + "] msgs=" + sb);
          }
        }
      }
    }
    pa.close();
  }
}
