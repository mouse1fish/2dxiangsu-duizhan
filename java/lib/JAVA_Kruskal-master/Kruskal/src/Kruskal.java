import java.io.IOException;
import java.util.*;



public class Kruskal {  
    private Set<String> points=new HashSet<String>();  
    private List<Edge> treeEdges=new ArrayList<Edge>();  
    public void buildTree() throws IOException{  
        MapBuilder builder=new MapBuilder();  
        TreeSet<Edge> edges=builder.build();  
        int pointNum=builder.getPointNum();  
        DisjSets disjsets = new DisjSets(pointNum);
        int set1, set2;

        for( int k = 1; k < pointNum; k *= 2 )
        {
            for( int j = 0; j + k < pointNum; j += 2 * k )
            {
                set1 = disjsets.find( j );
                set2 = disjsets.find( j + k );
                disjsets.union( set1, set2 );
            }
        }
        
        for(Edge edge:edges){  
            if(isCircle(edge)){  
                continue;  
            }else{
                treeEdges.add(edge);
                
                if(treeEdges.size()==pointNum-1){  
                    return;  
                }  
            }  
        }  
    }  
    public void printTreeInfo(){  
        int totalDistance=0;  
        for(Edge edge:treeEdges){  
            totalDistance+=edge.getDistance();  
            System.out.println(edge.toString() + "\t" + edge.getDistance());  
        }  
        System.out.println("Length:"+totalDistance);  
    }  
    private boolean isCircle(Edge edge){  
        int size=points.size();  
        if(!points.contains(edge.getStart())){  
            size++;  
        }  
        if(!points.contains(edge.getEnd())){  
            size++;  
        }  
        if(size==treeEdges.size()+1){  
            return true;  
        }else{  
            points.add(edge.getStart());  
            points.add(edge.getEnd());  
            return false;  
        }  
    }

}  