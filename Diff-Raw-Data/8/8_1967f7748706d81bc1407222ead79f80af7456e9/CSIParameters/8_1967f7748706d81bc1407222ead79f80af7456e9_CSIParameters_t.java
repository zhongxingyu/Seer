 package terminator.terminal.states;
 
 class CSIParameters {
         private static int current = 0;
         private static short[] parameters = new short[16];
 
         public void clear() {
                 current = 0;
                 parameters[0] = 0;
         }
 
         public void process(char c) {
                 if (current < parameters.length)
                         parameters[current] = (short)Math.min(10 * parameters[current] + (c - '0'), Short.MAX_VALUE - 1);
         }
 
         public void next() {
                 if (current >= parameters.length)
                         return;
                 current++;
                 if (current < parameters.length)
                         parameters[current] = 0;
         }
 
         public int count() {
                 return Math.min(current + 1, parameters.length);
         }
 
         public int get(int index, int defaultValue) {
                 if (index >= parameters.length || index > current || parameters[index] == 0)
                         return defaultValue;
                 return parameters[index];
         }
 
         public int get(int index) {
                 return get(index, 0);
         }
 
         public int getCount() {
                 return get(0, 1);
         }
 
         public int getInt(int index) {
                return get(index, 1) - 1;
         }
 
         public int getType() {
                 return get(0, 0);
         }
 
         public String toString() {
                 StringBuilder string = new StringBuilder("(");
                 for (int i = 0; i < count(); i++) {
                         string.append(get(i));
                         if (i + 1 < count())
                                 string.append(", ");
                 }
                 string.append(")");
                 return string.toString();
         }
 }
