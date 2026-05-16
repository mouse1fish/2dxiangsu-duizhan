    public class Edge implements Comparable<Edge>{  
        private String start;  
        private String end;  
        private int distance;  
        public Edge(String start,String end,int distance){  
            this.start=start;  
            this.end=end;  
            this.distance=distance;  
        }  
        public String getStart() {  
            return start;  
        }  
        public void setStart(String start) {  
            this.start = start;  
        }  
        public String getEnd() {  
            return end;  
        }  
        public void setEnd(String end) {  
            this.end = end;  
        }  
        public int getDistance() {  
            return distance;  
        }  
        public void setDistance(int distance) {  
            this.distance = distance;  
        }  
        @Override  
        public String toString() {  
            return start + "->" + end;  
        }  
        @Override  
        public int compareTo(Edge obj) {  
            int targetDis=obj.getDistance();  
            return distance>targetDis?1:(distance==targetDis?0:-1);  
        }  
    }  