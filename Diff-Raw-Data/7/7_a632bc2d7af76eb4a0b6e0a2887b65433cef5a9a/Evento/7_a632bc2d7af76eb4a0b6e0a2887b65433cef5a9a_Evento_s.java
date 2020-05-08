public class Evento
﻿{
 	protected double data;
 	protected double duracao;
 	protected double desvioPadrao;
 
 
 	public Evento()
 	{
 		this.data = 0.0;
 		this.duracao = 0.0;
 		this.desvioPadrao = 0.0;
 	}
 
 
 	public Evento(Evento E)
 	{
 		this.data = E.data;
 		this.duracao = E.duracao;
 		this.desvioPadrao = E.desvioPadrao;
 	}
 
 
 	public Evento(double data, double duracao, double desvioPadrao)
 	{
 		this.data = data;
 		this.duracao = duracao;
 		this.desvioPadrao = desvioPadrao;
 	}
 
 
 	public Evento(double data, double duracao)
 	{
 		this();
 		this.data = data;
 		this.duracao = duracao;
 	}
 
 
 	public void execucao()
 	{
 
 
 	}
 
 
 	public Evento gerarProximo()
 	{
 		Evento temp = new Evento();
 		/* Alterar */
 		return temp;
 	}
 
 
 	public final void setData(double data)
 	{
 		this.data = data;
 	}
 
 
 	public final void setDuracao(double duracao)
 	{
 		this.duracao = duracao;
 	}
 
 
 	public final void setDesvioPadrao(double desvioPadrao)
 	{
 		this.desvioPadrao = desvioPadrao;
 	}
 
 
 	public final double getData()
 	{
 		return this.data;
 	}
 
 
 	public final double getDuracao()
 	{
 		return this.duracao;
 	}
 
 
 	public final double getDesvioPadrao()
 	{
 		return this.desvioPadrao;
 	}
}
