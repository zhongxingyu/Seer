 import java.util.ArrayList;
 import java.util.List;
 
 public class LineNumber
 {
 	private List<Integer> _numbers;
 	
 	public LineNumber(Integer... numbers)
 	{
 		_numbers = new ArrayList<Integer>();
 		for (Integer number: numbers)
 			_numbers.add(number);
 	}
 	
 	public LineNumber(LineNumber other)
 	{
 		_numbers = new ArrayList<Integer>();
 		for (Integer number: other._numbers)
 			_numbers.add(number);
 	}
 	
 	public LineNumber(String line)
 	{
        _numbers = new ArrayList<Integer>();
 	   int _size;
        for (_size = 0; _size < line.length(); ++_size)
     	   if (!Character.isDigit(line.charAt(_size)) && line.charAt(_size) != '.')
     		   break;
       for (String nb : line.split("\\."))
        {
     	   Integer currentNumber = Integer.parseInt(nb);
            _numbers.add(currentNumber);
        }
 	}
 	
 	public List<Integer> number()
 	{
 		return _numbers;
 	}
 	
 	public void set(int index, Integer value)
 	{
 		_numbers.set(index, value);
 	}
 	
 	public void add(Integer value)
 	{
 		_numbers.add(value);
 	}
 	
 	public int compare(LineNumber rhs)
 	{
 		List<Integer> other = rhs.number();
 		for (int i = 0; i < _numbers.size() && i < other.size(); ++i)
 			if (_numbers.get(i) != other.get(i))
 			{
 				if (_numbers.get(i) < other.get(i))
 					return -1;
 				return 1;
 			}
 		if (_numbers.size() > other.size())
 			return 1;
 		else if (_numbers.size() < other.size())
 			return -1;
 		return 0;
 	}
 	
 	public int length()
 	{
 		return toString().length();
 	}
 	
 	@Override
 	public String toString()
 	{
 		String str = "";
 		for (int i = 0; i < _numbers.size(); ++i)
 			str = str.concat(_numbers.get(i).toString() + ".");
 		str = str.substring(0, str.length() - 1);
 		return str;
 	}
 }
