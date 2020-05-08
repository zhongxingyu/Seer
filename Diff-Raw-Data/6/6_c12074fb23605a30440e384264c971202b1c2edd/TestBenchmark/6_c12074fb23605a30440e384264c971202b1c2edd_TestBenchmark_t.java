 package radlab.rain.workload.http;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import radlab.rain.agent.AgentPOL;
 import radlab.rain.agent.IAgent;
 import radlab.rain.agent.IAgentFactory;
 import radlab.rain.operation.Generator;
 import radlab.rain.operation.IGeneratorFactory;
 import radlab.rain.target.DefaultTarget;
 import radlab.rain.target.ITarget;
 import radlab.rain.target.ITargetFactory;
 
 public class TestBenchmark implements ITargetFactory, IGeneratorFactory, IAgentFactory {
 
 	private long targetCount;
 	private JSONObject targetConfig;
 	private String baseUrl;
 
 	@Override
 	public void configure(JSONObject params) throws JSONException {
 		targetCount = params.getInt("targetCount");
 		targetConfig = params.getJSONObject("targetConfig");
 	}
 
 	@Override
	public List<ITarget> createTargets(String hostname) throws JSONException {
		this.baseUrl = hostname;

 		List<ITarget> tracks = new LinkedList<ITarget>();
 		for (int i = 0; i < targetCount; i++) {
 			tracks.add(createTarget());
 		}
 		return tracks;
 	}
 
 	protected ITarget createTarget() {
 		DefaultTarget target = new DefaultTarget();
 		try {
 			target.configure(targetConfig);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		target.setLoadScheduleFactory(new TestScheduleCreator());
 		target.setGeneratorFactory(this);
 		target.setAgentFactory(this);
 		return target;
 	}
 
 	@Override
 	public Generator createGenerator() {
 		TestGenerator generator = new TestGenerator(baseUrl);
 		return generator;
 	}
 
 	@Override
 	public IAgent createAgent(long targetId, long id) {
 		AgentPOL agent = new AgentPOL(targetId, id);
 		return agent;
 	}
 }
