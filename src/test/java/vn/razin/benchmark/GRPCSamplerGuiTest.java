package vn.razin.benchmark;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

@Ignore
public class GRPCSamplerGuiTest {

  @BeforeClass
  public static void setUpClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void showGui() throws Exception {
    if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
      GRPCSamplerGui gui = new GRPCSamplerGui();
      JDialog frame = new JDialog();
      frame.add(gui);

      frame.setPreferredSize(new Dimension(800, 600));
      frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
      while (frame.isVisible()) {
        Thread.sleep(100);
      }
    }
  }
}
