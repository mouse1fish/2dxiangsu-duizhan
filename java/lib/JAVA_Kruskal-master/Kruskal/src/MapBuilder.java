import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

public class MapBuilder {  

	int line = 0;
        public TreeSet<Edge> build() throws IOException{  
        	
        	String[] edge = new String[1000];
        	BufferedReader infile = new BufferedReader (new FileReader ("assn9_data.csv"));

        	for (int i = 0 ; i < edge.length ; i++)
        	{
        		edge [i] = infile.readLine ();
        		
        	    if (edge [i] == null)
        		break;
        	    line++;
//        	    System.out.println (edge [i]);
//            	int number = (csvOperator.getNumber(edge[i]) - 1) / 2;
//            	System.out.println(number);
        	}
        	
            
        	TreeSet<Edge> edges=new TreeSet<Edge>();
            
            
            for(int hang = 22; hang > 0 ; hang --){
        	    if(edge[hang]!=null){
//            	    System.out.println (edge [hang]);
        	    	int number = (csvOperator.getNumber(edge[hang]) - 1) / 2;
                	for (int i = 0; i < number; i ++){
                		edges.add(new Edge(csvOperator.getString(edge[hang], 0),
                				csvOperator.getString(edge[hang], i*2+1),
                				Integer.parseInt(csvOperator.getString(edge[hang], i*2+2))));  
                	}
        	    }
            	
            }
            
            return edges;  
        }  
        public int getPointNum(){  
        	System.out.println("There are " + line + " points.");
            return line;  
        }  
    }  