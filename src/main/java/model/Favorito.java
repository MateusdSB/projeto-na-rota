package model;

public class Favorito {
    private int id;
    private String type;
    private String refId;
    private String label;

    public Favorito(){}

    public Favorito(String type, String refId, String label){
        this.type = type;
        this.refId = refId;
        this.label = label;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString(){
        return id + " - " + (label == null ? "" : label) + "(" + type +")";
    }
}
