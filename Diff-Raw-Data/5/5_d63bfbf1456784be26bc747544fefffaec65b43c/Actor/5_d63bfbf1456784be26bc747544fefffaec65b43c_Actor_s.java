 /* 
  * Copyleft (o) 2012 James Baiera
  * All wrongs reserved.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package STEngine;
 
 import java.util.HashMap;
 
 public class Actor extends Entity {
 
 	private HashMap<String, Animation> actionbank = new HashMap<String,Animation>();
 	
 	/**
 	 * Create an Actor with null animation at point x, y
 	 * 
 	 * @param x x coordinate of Actor
 	 * @param y y coordinate of Actor
 	 */
 	public Actor(int x, int y) {
 		super(x,y);
 		this.actionbank.put("NULL_ACTION", new NullAnimation(1000));
		this.sprt = actionbank.get("NULL_ANIMATION");
 	}
 	
 	/**
 	 * Adds an Animation to the list of Animations the Actor has
 	 */
 	public void addAnimation(String s, Animation a){
 		this.actionbank.put(s, a);
 	}
 	
 	public void setAnimation(String s){
 		this.sprt = actionbank.get(s);
 		if (this.sprt == null) {
			this.sprt = new NullAnimation(1000);
 		}
 	}
 }
