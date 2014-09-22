/**
 * Simple class used only for testing purposes.
 * @author aguirre
 *
 */
public class SimpleClass {

        public int x;

        public SimpleClass() {
                x = 0;
        }

        /*@ requires x == 0;
          @ ensures \result >= 0;
          @*/
        public int getX() {
                return (x-1); //mutGenLimit 1
        }
        
        /*@ requires newX >= 0;
        @ ensures x >= 0;
        @*/
      public void setX(int newX) {
              x = newX; //mutGenLimit 1
      }        

      /*@ requires newX >= 0;
      @ ensures \result >= 0;
      @*/
    public static int decX(int newX) {
            return (newX-1); //mutGenLimit 1
    }

    /*@ requires newX >= 0;
    @ ensures \result >= 0;
    @*/
  public static int decXTwice(int newX) {
          return ((newX-1)-1); //mutGenLimit 2
  }

    
    
    
}
