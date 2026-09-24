import com.just.print.simclient.LocalMovingInputs;
import org.cas.shared.inventory.MovingPlan;
import org.cas.client.platform.bar.dialog.todo.TodoDialog;
import org.json.*;

public class LocalMovingInputsCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static JSONObject product(int id, String name, int shortage) {
        return new JSONObject().put("id",id).put("mnemonic",name).put("subject",name).put("category","BIERE")
                .put("code","code"+id).put("stock1",0).put("stock2",0).put("stock3",48).put("monque",shortage);
    }
    public static void main(String[] args) throws Exception {
        String[] names={"ALPHA 5c 355x6","ALPHA 5c 355x4","ALPHA 5c 355x12","ALPHA 5c 355x15","ALPHA 5c 355x18",
                "ALPHA 5c 355x20","ALPHA 5c 355x24","ALPHA 5c 740","ALPHA 5c 710","BUDWEISER 5 PERCENT 12x355 ML CANS",
                "BUDWEISER 355x6","BUDWEISER ZERO 355x12","HEINEKEN 5.0B 330x6","HEINEKEN 5.0C 330x6","ALPHA 5c 355 06X","12345",""};
        for (String name:names) for (int explicit:new int[]{0,1,4,24}) {
            JSONObject p=product(1,name,4).put("case_qty",explicit);
            if (name.isEmpty() || name.equals("12345")) p.put("subject","ALPHA 5c 355x6");
            JSONObject expected=TodoDialog.movingProductFacts(p), actual=LocalMovingInputs.facts(p);
            for (java.util.Iterator<String> it=expected.keys(); it.hasNext();) {
                String key=it.next(); check(expected.get(key).equals(actual.get(key)),name+" caseQty="+explicit+" "+key+": "+expected+" vs "+actual);
            }
        }
        JSONArray raw=new JSONArray().put(product(1,"ALPHA 5c 355x12",2)).put(product(2,"BETA 5c 740",24));
        JSONObject input=LocalMovingInputs.fromCatalog(raw);
        check(!input.getBoolean("salesAvailable"),"Legacy catalog reports missing sales, does not fabricate it");
        JSONObject draft=MovingPlan.generate(input,null);
        check(draft.getJSONArray("rows").length()==2,"Generate from ordinary saved products without server metadata or HTTP");
        draft.getJSONArray("rows").getJSONObject(0).put("state","done");
        draft.getJSONArray("rows").getJSONObject(1).put("state","skipped");
        JSONObject reopened=MovingPlan.generate(LocalMovingInputs.fromCatalog(new JSONArray(raw.toString())),new JSONObject(draft.toString()));
        check(reopened.getJSONArray("rows").getJSONObject(0).getString("state").equals("done"),"Completed state restored");
        check(reopened.getJSONArray("rows").getJSONObject(1).getString("state").equals("skipped"),"Removed state restored");
        raw.getJSONObject(0).put("monque",4);
        JSONObject changed=MovingPlan.generate(LocalMovingInputs.fromCatalog(raw),null);
        check(changed.getJSONArray("rows").getJSONObject(0).getLong("cases")==4,"Current phone Monque used");
        System.out.println("PASS: 68 desktop rule parity cases, legacy local-only generation, saved decisions, local Monque recalculation.");
    }
}
