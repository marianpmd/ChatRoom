package Main;

public class Listener implements MyObs {
    private String value ;



    public Listener() {
    }

    public Listener(String value) {
        this.value = value;
    }

    @Override
    public void update(Object o) {
        String value = (String) o;
        value = value.replaceAll("\n","");
        this.setValue(value);
        System.out.println("value = " +value);

    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
