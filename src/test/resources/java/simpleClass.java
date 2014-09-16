public class SimpleClass {

        private int x;

        public SimpleClass() {
                x = 10;
        }
        /*@
          @ ensures \result >= 0;
          @*/
        public int getX() {
                return x;
        }
}
