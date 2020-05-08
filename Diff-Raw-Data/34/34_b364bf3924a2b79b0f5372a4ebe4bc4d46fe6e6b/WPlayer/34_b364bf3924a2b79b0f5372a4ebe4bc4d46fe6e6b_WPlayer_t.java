 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ru.natty.web.server.wcontent;
 
 import ru.natty.persist.Track;
 import ru.natty.web.persist.Label;
 import ru.natty.web.server.DataBase;
 import ru.natty.web.server.WContentCreator;
 import ru.natty.web.shared.Parameters;
 import ru.natty.web.shared.diffpatchers.DiffPatcher;
 import ru.natty.web.shared.diffpatchers.PlayerDP;
 
 /**
  *
  * @author necto
  */
 public class WPlayer extends WContent
 {
 	private String url;
 
 	public WPlayer (String url)
 	{
 		this.url = url;
 	}
 
 	public String getUrl()
 	{
 		return url;
 	}
 
 	@Override
 	public DiffPatcher getDifferenceInt(WContent prev, boolean amputation)
 	{
		assert prev instanceof WPlayer;
 		if (url.equals(((WPlayer)prev).getUrl())) return null;
 		return new PlayerDP(url);
 	}
 
 	@Override
 	public DiffPatcher getAllContentInt()
 	{
 		return new PlayerDP (url);
 	}
 
 	@Override
 	public WContent copyInt()
 	{
 		return new WPlayer (url);
 	}
 
 	@Override
 	public boolean isAggregating() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "WPlayer:\"" + url + "\"";
 	}
 
 	public static WContent make (Integer id, Parameters ps, DataBase db, WContentCreator creator)
 	{
		String trId = ps.getVal("Track");
		if (trId.equals("")) return new WPlayer ("no track to play");
		Track t = db.queryTrackById(Integer.parseInt(trId));
        return new WPlayer(t.getUrl()).setStyle(id, db);
 	}
 }
