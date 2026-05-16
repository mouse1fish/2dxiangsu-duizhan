
public class csvOperator {
	
	public static int getNumber(String s){
		int separator = 0;
		String value[] =  s.split(",");
		return(value.length);
	}
	
	public static String getString(String s, int num){
		int separator = 0;
		String value[] =  s.split(",");
		return(value[num]);
	}
}
