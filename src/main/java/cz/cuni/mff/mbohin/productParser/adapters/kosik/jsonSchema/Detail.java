package cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema;

@SuppressWarnings("unused")
public class Detail {
    public boolean adultOnly;
    public Brand brand;
    public String sapId;
    public Object[] shoppingListIds;
    public String[] photos;
    public Supplierinfo[] supplierInfo;
    public Origin[] origin;
    public Description[] description;
    public Ingredient[] ingredients;
    public KosikNutritionalValues nutritionalValues;
    public Parametergroup[] parameterGroups;
    public Bestbefore bestBefore;
    public Object associationCode;
    public boolean unlisted;
    public Object metaDescription;
}
