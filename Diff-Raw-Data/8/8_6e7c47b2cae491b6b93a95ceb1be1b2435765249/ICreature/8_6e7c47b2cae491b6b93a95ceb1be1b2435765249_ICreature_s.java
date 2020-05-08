 package aigilas.creatures;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import aigilas.skills.SkillLogic;
 import spx.bridge.ActorType;
 import spx.bridge.DrawDepth;
 import spx.bridge.EntityType;
 import spx.core.Point2;
 import spx.entities.CoordVerifier;
 import spx.entities.Entity;
 import spx.entities.EntityManager;
 import spx.entities.IActor;
 import spx.io.Input;
 import spx.text.ActionText;
 import spx.text.ActionTextHandler;
 import spx.text.TextManager;
 import spx.util.IntStorage;
 import spx.util.StringStorage;
 import aigilas.classes.CreatureClass;
 import aigilas.entities.Elements;
 import aigilas.gods.God;
 import aigilas.gods.GodId;
 import aigilas.hud.HudManager;
 import aigilas.items.Equipment;
 import aigilas.items.GenericItem;
 import aigilas.items.Inventory;
 import aigilas.management.Commands;
 import aigilas.reactions.ComboMeter;
 import aigilas.skills.SkillId;
 import aigilas.skills.SkillPool;
 import aigilas.statuses.IStatus;
 import aigilas.statuses.StatusComponent;
 import aigilas.statuses.StatusPool;
 import aigilas.strategies.IStrategy;
 import aigilas.strategies.Strategy;
 import aigilas.strategies.TargetSet;
 
 public abstract class ICreature extends Entity implements IActor {
 	protected IStrategy _strategy;
 
 	protected CreatureClass _class;
 	protected Stats _baseStats;
 	protected Stats _maxStats;
 	protected God _god;
 	protected ICreature _master;
 	protected List<Elements> _composition = new ArrayList<>();
 
 	protected SkillPool _skills;
 	protected Point2 _skillVector = new Point2(0, 0);
 	protected ComboMeter _combo;
 	protected StatusPool _statuses = new StatusPool();
 
 	protected Inventory _inventory;
 	protected Equipment _equipment;
 
 	protected HudManager _hudManager;
 	protected ActionTextHandler _damageText = new ActionTextHandler();
 
 	protected int _playerIndex = -1;
 	protected boolean _isPlaying = true;
 	protected int _currentLevel = 1;
 	protected float _experience;
 	protected static final float __levelUpAmonut = 50;
 	protected float _nextLevelExperience = __levelUpAmonut;
 
 	protected ActorType _actorType;
 
 	protected void Setup(Point2 location, ActorType type, Stats stats, CreatureClass creatureClass, boolean setClass) {
 		_entityType = EntityType.ACTOR;
 		Initialize(location, type.Sprite, EntityType.ACTOR, DrawDepth.Creature);
 		Init(type, stats, creatureClass, setClass);
 	}
 
 	protected void Setup(Point2 location, ActorType type, Stats stats, CreatureClass creatureClass) {
 		Setup(location, type, stats, creatureClass, true);
 	}
 
 	protected void Setup(Point2 location, ActorType type, Stats stats) {
 		Setup(location, type, stats, null, true);
 	}
 
 	protected void SetClass(CreatureClass cClass) {
 		if (_class != cClass || cClass == null || cClass == CreatureClass.NULL) {
 			_class = (cClass == null) ? CreatureClass.NULL : cClass;
 			if (_skills == null) {
 				_skills = new SkillPool(this);
 			}
 			for (SkillId skillId : SkillLogic.GetElementalSkills(GetActorType(), _composition)) {
 				_skills.Add(skillId);
 			}
 			_skills.Add(_class.GetLevelSkills(_currentLevel));
 		}
 	}
 
 	private void Init(ActorType type, Stats stats, CreatureClass creatureClass, boolean setClass) {
 		if (setClass) {
 			SetClass(creatureClass);
 		}
 		_inventory = new Inventory(this);
 		_equipment = new Equipment(this);
 		_combo = new ComboMeter(this);
 		if (_playerIndex > -1) {
 			_hudManager = new HudManager(this, _inventory, _equipment);
 		}
 		if (_isBlocking == null) {
 			_isBlocking = true;
 		}
 		_actorType = type;
 		_baseStats = new Stats(stats);
 		_maxStats = new Stats(_baseStats);
 	}
 
 	public void PickupItem(GenericItem item) {
 		_inventory.Add(item);
 		EntityManager.RemoveObject(item);
 	}
 
 	public void Equip(GenericItem item) {
 		if (_inventory.GetItemCount(item) > 0 && !_equipment.IsRegistered(item)) {
 			_equipment.Register(item);
 			_inventory.Remove(item);
 		}
 		else {
 			if (_equipment.IsRegistered(item)) {
 				_equipment.Unregister(item);
 			}
 		}
 	}
 
 	public void Drop(GenericItem item) {
 		if (item != null) {
 
 			if (_inventory.GetItemCount(item) > 0) {
 				EntityManager.addObject(new GenericItem(item, GetLocation()));
 				_inventory.Remove(item);
 			}
 			else {
 				if (_inventory.GetItemCount(item) == 0) {
 					_equipment.Unregister(item);
 					EntityManager.addObject(new GenericItem(item, GetLocation()));
 					_inventory.Remove(item);
 				}
 			}
 		}
 	}
 
 	public GenericItem DestroyRandomItemFromInventory() {
 		GenericItem item = _inventory.GetNonZeroEntry();
 		if (item != null) {
 			_inventory.Remove(item);
 		}
 		return item;
 	}
 
 	public boolean IsCooledDown() {
 		return Get(StatType.MOVE_COOL_DOWN) >= GetMax(StatType.MOVE_COOL_DOWN);
 	}
 
 	@Override
 	public void Update() {
 		_statuses.Update();
 		if (Get(StatType.HEALTH) <= 0) {
 			_isActive = false;
 		}
 		if (_statuses.Allows(CreatureAction.Movement)) {
 			if (_isPlaying) {
 				if (!IsCooledDown()) {
 					Adjust(StatType.MOVE_COOL_DOWN, 1);
 				}
 				else {
 					_statuses.Act();
 				}
 				Regenerate();
 			}
 			if (_strategy != null) {
 				_strategy.Act();
 				_combo.Update();
 			}
 		}
 		if (_hudManager != null) {
 			_hudManager.Update();
 		}
 		_damageText.Update();
 	}
 
 	private void Regenerate() {
 		if (_statuses.Allows(CreatureAction.Regeneration)) {
 			for (StatType stat : StatType.values()) {
 				if (stat != StatType.MOVE_COOL_DOWN && stat != StatType.REGEN) {
 					if (_baseStats.GetRaw(stat) < _maxStats.GetRaw(stat)) {
 						Adjust(stat, _baseStats.Get(StatType.REGEN) / 50);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void Draw() {
 		super.Draw();
 		if (_hudManager != null) {
 			_hudManager.Draw();
 		}
 		_combo.Draw();
 		_damageText.Draw();
 	}
 
 	public void Write(String text) {
 		_damageText.WriteAction(text, 30, IntStorage.Get(GetLocation().PosCenterX), IntStorage.Get(GetLocation().PosCenterY));
 	}
 
 	public boolean ToggleInventoryVisibility() {
 		if (_hudManager != null) {
 			return _hudManager.ToggleInventory();
 		}
 		return false;
 	}
 
 	public void SetPlaying(boolean isPlaying) {
 		_isPlaying = isPlaying;
 	}
 
 	public boolean IsPlaying() {
 		return _isPlaying;
 	}
 
 	public float Get(StatType stat) {
 		return _baseStats.Get(stat) + CalculateEquipmentBonus(stat) + CalculateInstrinsicBonus(stat);
 	}
 
 	private float GetRaw(StatType stat, boolean isMax) {
 		return isMax ? _maxStats.GetRaw(stat) : _baseStats.GetRaw(stat);
 	}
 
 	private float GetRaw(StatType stat) {
 		return GetRaw(stat, false);
 	}
 
 	private float CalculateInstrinsicBonus(StatType stat) {
 		if (_class == null) {
 			return 0;
 		}
 		return _class.GetBonus(_currentLevel, stat);
 	}
 
 	private float CalculateEquipmentBonus(StatType stat) {
 		if (_equipment != null) {
 			return _equipment.CalculateBonus(stat);
 		}
 		return 0;
 	}
 
 	public float GetMax(StatType stat) {
 		return (int) _maxStats.Get(stat) + CalculateEquipmentBonus(stat) + CalculateInstrinsicBonus(stat);
 	}
 
 	public float SetBase(StatType stat, float value) {
 		return _baseStats.Set(stat, value);
 	}
 
 	public float SetMax(StatType stat, float value) {
 		return _maxStats.Set(stat, value);
 	}
 
 	public float Set(StatType stat, float value, boolean setMax) {
 		return setMax ? SetMax(stat, value) : SetBase(stat, value);
 	}
 
 	public float Set(StatType stat, float value) {
 		return Set(stat, value, false);
 	}
 
 	protected void InitStat(StatType stat, float value) {
 		_maxStats.Set(stat, value);
 		_baseStats.Set(stat, value);
 	}
 
 	public int GetPlayerIndex() {
 		return _playerIndex;
 	}
 
 	@Override
 	public ActorType GetActorType() {
 		return _actorType;
 	}
 
 	protected float Adjust(StatType stat, float adjustment, boolean adjustMax) {
 		float result = GetRaw(stat) + adjustment;
 		if (!adjustMax) {
 			if (result > GetMax(stat)) {
 				result = GetMax(stat);
 			}
 		}
 		return Set(stat, (result), adjustMax);
 	}
 
 	protected float Adjust(StatType stat, float adjustment) {
 		return Adjust(stat, adjustment, false);
 	}
 
 	public void ApplyDamage(float damage, ICreature attacker, boolean showDamage, StatType statType) {
 		if (attacker != null) {
 			attacker.PassOn(this, StatusComponent.Contagion);
 			this.PassOn(attacker, StatusComponent.Passive);
 		}
 		if (statType == null) {
 			damage -= _baseStats.Get(StatType.DEFENSE);
 		}
 		if (damage <= 0 && statType == null) {
 			damage = 0;
 		}
 		if (showDamage) {
 			_damageText.WriteAction(StringStorage.Get(damage), 30, IntStorage.Get(GetLocation().PosCenterX), IntStorage.Get(GetLocation().PosCenterY));
 		}
 		if (damage > 0 && statType == null && _statuses.Allows(CreatureAction.ReceiveHealing)) {
 			Adjust((statType == null) ? StatType.HEALTH : statType, -damage);
 		}
 		if (Get(StatType.HEALTH) <= 0) {
 			_isActive = false;
 			if (attacker != null) {
 				attacker.AddExperience(CalculateExperience());
 				attacker.PassOn(attacker, StatusComponent.KillReward);
 			}
 		}
 	}
 
 	public void ApplyDamage(float damage, ICreature attacker, boolean showDamage) {
		ApplyDamage(damage, attacker, showDamage);
 	}
 
 	public void ApplyDamage(float damage, ICreature attacker) {
 		ApplyDamage(damage, attacker, true, null);
 	}
 
 	public void ApplyDamage(float damage) {
 		ApplyDamage(damage, null, true, null);
 	}
 
 	public boolean LowerStat(StatType stat, float amount) {
 		if (amount != 0) {
 			if (Get(stat) >= amount) {
 				Adjust(stat, -amount);
 				return true;
 			}
 			return false;
 		}
 		return true;
 	}
 
 	public void AddBuff(StatBuff buff, boolean applyToMax) {
 		if (!applyToMax) {
 			_baseStats.AddBuff(buff);
 		}
 		else {
 			_maxStats.AddBuff(buff);
 		}
 	}
 
 	public void AddBuff(StatBuff buff) {
 		AddBuff(buff, false);
 	}
 
 	protected float CalculateDamage() {
 		return Get(StatType.STRENGTH);
 	}
 
 	private final Point2 target = new Point2(0, 0);
 	private final List<ICreature> creatures = new ArrayList<ICreature>();
 
 	public void MoveIfPossible(float xVel, float yVel) {
 
 		if (_statuses.Allows(CreatureAction.Movement)) {
 			if ((xVel != 0 || yVel != 0) && IsCooledDown()) {
 				target.Reset(xVel + GetLocation().PosX, yVel + GetLocation().PosY);
 				if (!IsBlocking() || !CoordVerifier.IsBlocked(target)) {
 					Move(xVel, yVel);
 					Set(StatType.MOVE_COOL_DOWN, 0);
 				}
 				if (_statuses.Allows(CreatureAction.Attacking)) {
 					creatures.clear();
 					for (IActor actor : EntityManager.GetActorsAt(target)) {
 						creatures.add((ICreature) actor);
 					}
 					if (creatures.size() > 0) {
 						for (ICreature creature : creatures) {
 							if (creature != this) {
 								if ((creature.GetActorType() != ActorType.PLAYER && _actorType == ActorType.PLAYER) || (creature.GetActorType() == ActorType.PLAYER && _actorType != ActorType.PLAYER) || !_statuses.Allows(CreatureAction.WontHitNonTargets)) {
 									creature.ApplyDamage(CalculateDamage(), this);
 									if (!creature.IsActive()) {
 										AddExperience(creature.CalculateExperience());
 									}
 								}
 							}
 						}
 						Set(StatType.MOVE_COOL_DOWN, 0);
 					}
 				}
 			}
 		}
 	}
 
 	public void MoveTo(Point2 targetPosition) {
 		MoveIfPossible(targetPosition.PosX - GetLocation().PosX, targetPosition.PosY - GetLocation().PosY);
 	}
 
 	public Point2 GetSkillVector() {
 		if (null == _skillVector) {
 			return null;
 		}
 		return _skillVector;
 	}
 
 	public void SetSkillVector(Point2 skillVector) {
 		_skillVector.SetX(skillVector.X);
 		_skillVector.SetY(skillVector.Y);
 	}
 
 	public void AddExperience(float amount) {
 
 		while (amount > 0) {
 			float diff = amount;
 			if (amount > _nextLevelExperience) {
 				diff = _nextLevelExperience;
 				amount -= _nextLevelExperience;
 			}
 			else {
 				amount = 0;
 			}
 			_experience += diff;
 			if (_experience > _nextLevelExperience) {
 				_nextLevelExperience += __levelUpAmonut;
 				_experience = 0;
 				_currentLevel++;
 				if (_class != null) {
 					_skills.Add(_class.GetLevelSkills(_currentLevel));
 				}
 				TextManager.Add(new ActionText("LEVEL UP!", 100, (int) GetLocation().PosX, (int) GetLocation().PosY));
 			}
 		}
 	}
 
 	public float CalculateExperience() {
 		return _currentLevel + _baseStats.GetSum();
 	}
 
 	public void CycleActiveSkill(int velocity) {
 		if (_statuses.Allows(CreatureAction.SkillCycle)) {
 			_skills.Cycle(velocity);
 		}
 	}
 
 	public String GetActiveSkillName() {
 		return _skills.GetActiveName();
 	}
 
 	float lastSum = 0;
 
 	public void UseActiveSkill() {
 		if (_statuses.Allows(CreatureAction.SkillUsage)) {
 			lastSum = _baseStats.GetSum();
 			_skills.UseActive();
 			if (lastSum != _baseStats.GetSum()) {
 				_damageText.WriteAction(GetActiveSkillName(), 40, IntStorage.Get(GetLocation().PosCenterX), IntStorage.Get(GetLocation().PosY));
 			}
 		}
 	}
 
 	public TargetSet GetTargets() {
 		if (_strategy == null) {
 			return _master.GetTargets();
 		}
 		return _strategy.GetTargets();
 	}
 
 	private StatType GetLowestStat() {
 		StatType result = StatType.AGE;
 		float min = Float.MAX_VALUE;
 		List<StatType> possibleStats = new ArrayList<>();
 		for (StatType stat : StatType.values()) {
 			if (Get(stat) < min && stat != StatType.AGE && stat != StatType.MOVE_COOL_DOWN && stat != StatType.PIETY) {
 				possibleStats.add(stat);
 			}
 		}
 		for (StatType stat : possibleStats) {
 			result = stat;
 			min = Get(stat);
 		}
 		return result;
 	}
 
 	public void Sacrifice(God god, GenericItem sacrifice) {
 		AssignGod(god);
 		Adjust(StatType.PIETY, sacrifice.Modifers.GetSum() * ((_god.IsGoodSacrifice(sacrifice.GetItemClass())) ? 3 : 1) * ((_god.IsBadSacrifice(sacrifice.GetItemClass())) ? -2 : 1), true);
 		Adjust(StatType.PIETY, sacrifice.Modifers.GetSum() * ((_god.IsGoodSacrifice(sacrifice.GetItemClass())) ? 3 : 1) * ((_god.IsBadSacrifice(sacrifice.GetItemClass())) ? -2 : 1));
 		sacrifice.SetInactive();
 	}
 
 	static final int pietyCost = 500;
 
 	public void Pray(God god) {
 		AssignGod(god);
 		if (Get(StatType.PIETY) >= pietyCost) {
 			StatType lowest = GetLowestStat();
 			float adjustment = (Get(StatType.PIETY) / 100);
 			Set(lowest, GetMax(lowest) + adjustment);
 			Set(lowest, adjustment, true);
 			Adjust(StatType.PIETY, -pietyCost);
 			if (Get(StatType.PIETY) < 0) {
 				Set(StatType.PIETY, 0);
 			}
 		}
 		PerformInteraction();
 	}
 
 	protected void AssignGod(God god) {
 		if (_god != god && _god != null) {
 			ApplyDamage(Get(StatType.PIETY));
 			Set(StatType.PIETY, 0);
 			SetClass(god.GetClass());
 		}
 		_god = god;
 	}
 
 	public void Combo(List<Elements> attack) {
 		for (Elements element : attack) {
 			_combo.Add(element);
 		}
 	}
 
 	public void AddStatus(IStatus status) {
 		_statuses.Add(status);
 	}
 
 	public void PassOn(ICreature target, StatusComponent componentType) {
 		_statuses.PassOn(target, componentType);
 	}
 
 	public void SetStrategy(IStrategy strategy) {
 		_strategy = strategy;
 	}
 
 	public Strategy GetStrategyId() {
 		return _strategy.GetId();
 	}
 
 	public float GetInventoryCount() {
 		return _inventory.NonZeroCount();
 	}
 
 	public void RemoveLeastUsedSkill() {
 		_skills.RemoveLeastUsed();
 	}
 
 	public void React(SkillId skillId) {
 		if (_actorType == ActorType.PLAYER && skillId != SkillId.FORGET_SKILL && _god.Id == GodId.GLUTTONY) {
 			if (_skills.Count() < _currentLevel) {
 				_skills.Add(skillId);
 			}
 		}
 	}
 
 	@Override
 	public void PerformInteraction() {
 		SetInteracting(false);
 		Input.Lock(Commands.Confirm, GetPlayerIndex());
 	}
 
 	public void MarkHotSkill(Commands hotSkillSlot) {
 		_skills.MakeActiveSkillHot(hotSkillSlot);
 	}
 
 	public boolean SetHotSkillActive(Commands hotkey) {
 		return _skills.SetHotSkillsActive(hotkey);
 	}
 
 	public String GetHotSkillName(Commands hotSkillSlot) {
 		return _skills.GetHotSkillName(hotSkillSlot);
 	}
 
 	public List<ActorType> GetTargetActorTypes() {
 		return _strategy.GetTargetActorTypes();
 	}
 
 	public float GetCurrentSkillCost() {
 		return _skills.GetCurrentCost();
 	}
 
 	public SkillId GetActiveSkill() {
 		return _skills.GetActive();
 	}
 }
