package art.lookingup.patterns.play;

public abstract class BaseFactory implements FragmentFactory {
  private final String fragName;
  public FragmentFactory parent;

  public BaseFactory(String fragName, FragmentFactory... children) {
    this.fragName = fragName;
    for (FragmentFactory child : children) {
      child.setParent(this);
    }
  }

  private BaseFactory() {
    this.fragName = "impossible";
  }

  public String toString() {
    if (this.parent == null) {
      return fragName;
    }
    if (this.fragName == "") {
      return parent.toString();
    }
    return parent.toString() + "." + fragName;
  }

  @Override
  public void setParent(FragmentFactory parent) {
    this.parent = parent;
  }
};
