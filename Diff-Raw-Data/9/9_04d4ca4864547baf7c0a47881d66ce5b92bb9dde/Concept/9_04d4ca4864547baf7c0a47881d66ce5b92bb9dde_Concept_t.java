 package dhbw.LWBS.CA5_KB1.model;
 
import java.util.Map;

 public class Concept extends Person
 {
 	Concept(AgeClass ageClass, Gender gender, Married married,
 			Children children, Degree degree, Profession profession,
 			Income income)
 	{
 		super(0, ageClass, gender, married, children, degree, profession,
 				income, Book.NONE);
 	}
 
 	public static Concept getMostSpecializedConcept()
 	{
 		return new Concept(AgeClass.NONE, Gender.NONE, Married.NONE,
 				Children.NONE, Degree.NONE, Profession.NONE, Income.NONE);
 	}
 
 	public static Concept getMostGeneralizedConcept()
 	{
 		return new Concept(AgeClass.ALL, Gender.ALL, Married.ALL, Children.ALL,
 				Degree.ALL, Profession.ALL, Income.ALL);
 	}
 
 	public boolean covers(Person p)
 	{
 		for (String attributeKey : attributes.keySet())
 		{
 			int this_Attribute = attributes.get(attributeKey);
			Map<String,Integer> att = p.getAttributes();
			int other_Attribute = att.get(attributeKey);
 			
			if ((this_Attribute != other_Attribute) && (this_Attribute != 100))
 				return false;
 		}
 		return true;
 	}
 	
 	public Concept copy()
 	{
 		Concept temp = new Concept(AgeClass.fromInteger(ageClass.getId()), 
 				Gender.fromInteger(gender.getId()),
 				Married.fromInteger(married.getId()),
 				Children.fromInteger(children.getId()),
 				Degree.fromInteger(degree.getId()), 
 				Profession.fromInteger(profession.getId()), 
 				Income.fromInteger(income.getId()));
 		
 		return temp;
 	}
 	
 	public void setAttribute (String name, Integer attribute)
 	{
 		if (name.equals(AgeClass.NAME))
 			setAgeClass(AgeClass.fromInteger(attribute));
 		else if (name.equals(Gender.NAME))
 			setGender(Gender.fromInteger(attribute));
 		else if (name.equals(Married.NAME))
 			setMarried(Married.fromInteger(attribute));
 		else if (name.equals(Children.NAME))
 			setChildren(Children.fromInteger(attribute));
 		else if (name.equals(Degree.NAME))
 			setDegree(Degree.fromInteger(attribute));
 		else if (name.equals(Profession.NAME))
 			setProfession(Profession.fromInteger(attribute));
 		else if (name.equals(Income.NAME))
 			setIncome(Income.fromInteger(attribute));
 		else
 			System.err.println("setAttribute called with unknown attribute name: " + name);
 	}
 	
 	public boolean equals(Object obj)
 	{
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Person))
 			return false;
 		Person other = (Person) obj;
 		
 		if (ageClass != other.ageClass)
 			return false;
 		if (children != other.children)
 			return false;
 		if (degree != other.degree)
 			return false;
 		if (gender != other.gender)
 			return false;
 		if (income != other.income)
 			return false;
 		if (married != other.married)
 			return false;
 		if (profession != other.profession)
 			return false;
 		return true;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return super.toConceptString();
 	}
 
 	/**
 	 * @param ageClass
 	 *            the ageClass to set
 	 */
 	public void setAgeClass(AgeClass ageClass)
 	{
 		this.ageClass = ageClass;
 		this.attributes.put(AgeClass.NAME, this.ageClass.getId());
 	}
 
 	/**
 	 * @param gender
 	 *            the gender to set
 	 */
 	public void setGender(Gender gender)
 	{
 		this.gender = gender;
 		this.attributes.put(Gender.NAME, this.gender.getId());
 	}
 
 	/**
 	 * @param married
 	 *            the married to set
 	 */
 	public void setMarried(Married married)
 	{
 		this.married = married;
 		this.attributes.put(Married.NAME, this.married.getId());
 	}
 
 	/**
 	 * @param children
 	 *            the children to set
 	 */
 	public void setChildren(Children children)
 	{
 		this.children = children;
 		this.attributes.put(Children.NAME, this.children.getId());
 	}
 
 	/**
 	 * @param degree
 	 *            the degree to set
 	 */
 	public void setDegree(Degree degree)
 	{
 		this.degree = degree;
 		this.attributes.put(Degree.NAME, this.degree.getId());
 	}
 
 	/**
 	 * @param profession
 	 *            the profession to set
 	 */
 	public void setProfession(Profession profession)
 	{
 		this.profession = profession;
 		this.attributes.put(Profession.NAME, this.profession.getId());
 	}
 
 	/**
 	 * @param income
 	 *            the income to set
 	 */
 	public void setIncome(Income income)
 	{
 		this.income = income;
 		this.attributes.put(Income.NAME, this.income.getId());
 	}
 }
