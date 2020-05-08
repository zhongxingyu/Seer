 package no.runsafe.creativetoolbox.database;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.IValue;
 import no.runsafe.framework.api.database.Repository;
 
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class PlotTagRepository extends Repository
 {
 	public PlotTagRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	public List<String> getTags(String plot)
 	{
 		List<IValue> tags = database.QueryColumn(
 			"SELECT `tag` FROM creative_plot_tags WHERE name=?",
 			plot
 		);
 		if (tags == null)
 			return null;
 		return Lists.transform(tags, new Function<IValue, String>()
 		{
 			@Override
 			public String apply(@Nullable IValue tag)
 			{
 				return tag == null ? null : tag.String();
 			}
 		});
 	}
 
 	public boolean addTag(String plot, String tag)
 	{
 		return database.Update(
			"INSERT INTO creative_plot_tags (`name`,`tag`) VALUES (?, ?)" +
 				"ON DUPLICATE KEY UPDATE `tag`=VALUES(`tag`)"
 		) > 0;
 	}
 
 	public boolean setTags(String plot, List<String> tags)
 	{
		boolean success = database.Execute("DELETE FROM creative_plot_tags WHERE `name`=?", plot);
 		if (tags != null)
 			for (String tag : tags)
 				success = success && addTag(plot, tag);
 		return success;
 	}
 
 	public List<String> findPlots(String tag)
 	{
 		List<IValue> plots = database.QueryColumn(
			"SELECT `name` FROM creative_plot_tags WHERE `tag` LIKE ?",
 			tag
 		);
 		if (plots == null)
 			return null;
 		return Lists.transform(plots, new Function<IValue, String>()
 		{
 			@Override
 			public String apply(@Nullable IValue plot)
 			{
 				return plot == null ? null : plot.String();
 			}
 		});
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "creative_plot_tags";
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> revisions = new HashMap<Integer, List<String>>();
 		List<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE creative_plot_tags (" +
 				"`name` VARCHAR(255)," +
 				"`tag` VARCHAR(255)," +
 				"PRIMARY KEY(`name`,`tag`)" +
 				")"
 		);
 		revisions.put(1, sql);
 		return revisions;
 	}
 
 	private final IDatabase database;
 }
