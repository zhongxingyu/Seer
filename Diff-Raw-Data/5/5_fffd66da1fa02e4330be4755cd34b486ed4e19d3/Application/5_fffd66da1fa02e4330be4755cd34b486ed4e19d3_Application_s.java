 package controllers;
 
 import java.util.List;
 
 import models.SuNumber;
 import models.SuNumber.OneNumber;
 import models.SuNumber.RowNumber;
 import models.SudokuForm;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import utils.RandomCreater;
 import utils.RandomCreater.Level;
 import views.html.sudoku.finish;
 import views.html.sudoku.form;
 import check.SudokuCheck;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 import static play.data.Form.*;
 
 public class Application extends Controller {
 
 	final static Form<SudokuForm> sudoku = form(SudokuForm.class);
 
 	final static Form<SuNumber> suForm = form(SuNumber.class);
 
 	/**
 	 * 初期画面はノーマルを作成
 	 * 
 	 * @return
 	 */
 	public static Result sudoku() {
 		SuNumber number = createSuNumbers(Level.NORMAL);
 		return ok(form.render("", suForm.fill(number)));
 	}
 
 	public static Result newSudoku() {
 		SuNumber number = createSuNumbers(Level.NORMAL);
 		return ok(form.render("", suForm.fill(number)));
 	}
 
 	public static Result expart() {
 		SuNumber number = createSuNumbers(Level.EXPART);
 		return ok(form.render("", suForm.fill(number)));
 	}
 
 	public static Result hard() {
 		SuNumber number = createSuNumbers(Level.HARD);
 		return ok(form.render("", suForm.fill(number)));
 	}
 
 	/**
 	 * レベルに応じた数独を作成
 	 * 
 	 * @param level
 	 * @return
 	 */
 	private static SuNumber createSuNumbers(Level level) {
 		RandomCreater creater = new RandomCreater(level);
 		int[] main = creater.make();
 
 		List<List<String>> numbers = toList(main);
 
 		SuNumber number = createSuNumber(numbers);
 		return number;
 	}
 
 	public static Result check() {
 		Form<SuNumber> filledForm = suForm.bindFromRequest();
 		SuNumber suNumber = filledForm.get();
		System.out.println("★★★★");
		suNumber.out();
 		suNumber.nullToEmpty();
		suNumber.readOnly();
		System.out.println("debuuuuuuu");
 
 		String message = "OKです";
 		if (SudokuCheck.check(suNumber) == false) {
 			message = "NG";
 		}
 
 		if (SudokuCheck.finish(suNumber)) {
 			return ok(finish.render("おめでとう！！！、完成です。"));
 		}
 
 		System.out.println(message);
 
 		return ok(form.render(message, suForm.fill(suNumber)));
 	}
 
 	static Function<String, OneNumber> toOneNumber = new Function<String, OneNumber>() {
 		@Override
 		public OneNumber apply(String arg0) {
 			return new OneNumber(arg0);
 		}
 	};
 
 	private static List<List<String>> toList(int[] main) {
 		List<Integer> list = Lists.newArrayList();
 		for (int i : main) {
 			list.add(i);
 		}
 
 		List<List<Integer>> newArrayList = Lists.newArrayList();
 		int start = 0;
 		int end = 1;
 		for (int i = 0; i < 9; i++) {
 			newArrayList.add(list.subList(9 * start++, 9 * end++));
 		}
 
 		List<List<String>> result = Lists.newArrayList();
 		for (List<Integer> list2 : newArrayList) {
 			List<String> in = Lists.newArrayList();
 			for (Integer integer : list2) {
 				int i = integer;
 				if (i == 0) {
 					in.add("");
 				} else {
 					in.add(integer.toString());
 				}
 			}
 			result.add(in);
 		}
 		return result;
 	}
 
 	private static SuNumber createSuNumber(List<List<String>> numbers) {
 
 		List<RowNumber> row = Lists.newArrayList();
 		for (List<String> list : numbers) {
 			row.add(new RowNumber(Collections2.transform(list, toOneNumber).toArray(new OneNumber[0])));
 		}
 
 		SuNumber suNumber = new SuNumber(row.toArray(new RowNumber[0]));
 		suNumber.readOnly();
 
 		return suNumber;
 	}
 }
