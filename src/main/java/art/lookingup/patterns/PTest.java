package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class PTest extends PanelBase {
  DiscreteParameter layerNum = new DiscreteParameter("layerNum", 0, -1, 30);
  DiscreteParameter panelNum = new DiscreteParameter("panelNum", 0, -1, 16);
  DiscreteParameter sixteenthNum = new DiscreteParameter("sixteenthNum", 0, -1, 16);
  BooleanParameter layersKnob = new BooleanParameter("layers", true);


  public PTest(LX lx) {
    super(lx);
    addParameter(layerNum);
    addParameter(panelNum);
    addParameter(layersKnob);
    addParameter(sixteenthNum);
  }

  public void processPanelPoint(Panel panel, int globalPanelNum) {
    if (layersKnob.getValueb()) {
      int whichLayer = layerNum.getValuei();
      if (ConeDownModel.isPanelInLayer(panel, whichLayer) && (panel.panelNum == panelNum.getValuei() || panelNum.getValuei() == -1)) {
        for (CXPoint p : panel.getPoints()) {
          colors[p.index] = Colors.WHITE;
        }
      } else {
        for (CXPoint p : panel.getPoints()) {
          colors[p.index] = Colors.BLACK;
        }
      }
    } else {
      // Do Sixteenths
      int whichSixteenth = sixteenthNum.getValuei();
      if (ConeDownModel.isPanelInSixteenth(panel, whichSixteenth) && (panel.panelNum == panelNum.getValuei() || panelNum.getValuei() == -1)) {
        for (CXPoint p : panel.getPoints()) {
          colors[p.index] = Colors.WHITE;
        }
      } else {
        for (CXPoint p : panel.getPoints()) {
          colors[p.index] = Colors.BLACK;
        }
      }
    }
  }
}
