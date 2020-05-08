 package avant.view.model;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EventListener;
 import java.util.List;
 import javax.swing.event.EventListenerList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author marano
  */
 public abstract class AvantAbstractModel<T> implements NotifiableList {
 
     private List<T> objetos;
     private List<T> objetosFiltrados;
     private boolean ordenada;
     private List<T> removidos = new ArrayList<T>();
     private T objetoSelecionado;
     private Comparator<T> comparador;
     private boolean filtrarAposCarregar = true;
     private boolean exibirMensagemErro = true;
     private boolean capturarErros = true;
     private boolean novaListaAoFiltrar = true;
     protected final EventListenerList listeners = new EventListenerList();
     protected final ControladorAcao controladorAcao = new ControladorAcao();
     protected final ControladorSelecaoLista controladorSelecaoLista = new ControladorSelecaoLista();
     protected final ControladorSelecaoItem controladorSelecaoItem = new ControladorSelecaoItem();
     public final ListenerAcao listenerAcao = new ListenerAcao();
     public final ListenerAdicao listenerAdicao = new ListenerAdicao();
     public final ListenerEdicao listenerEdicao = new ListenerEdicao();
     public final ListenerRemocao listenerRemocao = new ListenerRemocao();
     private final List<T> listaVazia = new ArrayList<T>();
 
     public AvantAbstractModel() {
         this(null);
     }
 
     public AvantAbstractModel(List<T> lista) {
         this(lista, true);
     }
 
     public AvantAbstractModel(boolean ordenada) {
         this(null, ordenada);
     }
 
     public AvantAbstractModel(final List<T> lista, boolean ordenada) {
         this(lista, ordenada, false);
     }
 
     public AvantAbstractModel(final List<T> lista, boolean ordenada, boolean esperarCarregar) {
         setOrdenada(ordenada, false);
         if (esperarCarregar) {
             setObjetos(lista != null ? lista : capturarErros ? tentarGetCarregarObjetos() : getCarregarObjetos(), false);
             aposCarregar(objetos);
         } else {
             setObjetos(listaVazia);
             new Thread(new Runnable() {
 
                 public void run() {
                     setObjetos(lista != null ? lista : capturarErros ? tentarGetCarregarObjetos() : getCarregarObjetos(), false);
                     if (listaVazia.size() > 0) {
                         objetos.addAll(listaVazia);
                         listaVazia.clear();
                     }
                     aposCarregar(objetos);
                 }
             }, "Carregamento " + this).start();
         }
     }
 
     public boolean isFiltrarAposCarregar() {
         return filtrarAposCarregar;
     }
 
     public void setFiltrarAposCarregar(boolean filtrarAposCarregar) {
         this.filtrarAposCarregar = filtrarAposCarregar;
     }
 
     final protected List<T> tentarGetFiltrar(List<T> lista) {
         try {
             return getFiltrar(lista);
         } catch (Throwable t) {
             tratarExcecao(t);
             return null;
         }
     }
 
     protected List<T> getFiltrar(List<T> lista) {
         return lista;
     }
 
     final public void filtrar() {
         filtrar(true);
     }
 
     final public void filtrar(boolean notificar) {
         List<T> listaFiltrada;
         if (capturarErros) {
             listaFiltrada = novaListaAoFiltrar ? tentarGetFiltrar(new ArrayList<T>(objetos)) : tentarGetFiltrar(objetos);
         } else {
             listaFiltrada = novaListaAoFiltrar ? getFiltrar(new ArrayList<T>(objetos)) : getFiltrar(objetos);
         }
         aposFiltrar(listaFiltrada);
         setObjetosFiltrados(listaFiltrada, notificar);
     }
 
     final public void setComparador(Comparator<T> comparador) {
         this.comparador = comparador;
         if (isOrdenada()) {
             ordenar(false, true);
         }
     }
 
     @Override
     public String toString() {
        return this.getClass().getSimpleName();//+ "<" + getClasse().getSimpleName() + ">";
     }
 
     protected void aposFiltrar(List<T> objetosFiltrados) {
     }
 
     final public boolean isCapturarErros() {
         return capturarErros;
     }
 
     final public void setCapturarErros(boolean capturarErros) {
         this.capturarErros = capturarErros;
     }
 
     final public boolean isNovaListaAoFiltrar() {
         return novaListaAoFiltrar;
     }
 
     final public void setNovaListaAoFiltrar(boolean novaListaAoFiltrar) {
         this.novaListaAoFiltrar = novaListaAoFiltrar;
     }
 
     final public boolean isExibirMensagemErro() {
         return exibirMensagemErro;
     }
 
     final public void setExibirMensagemErro(boolean exibirMensagemErro) {
         this.exibirMensagemErro = exibirMensagemErro;
     }
 
     final protected List<T> tentarGetCarregarObjetos() {
         try {
             return getCarregarObjetos();
         } catch (Throwable t) {
             tratarExcecao(t);
             return null;
         }
     }
 
     protected List<T> getCarregarObjetos() {
         return new ArrayList<T>();
     }
 
     final public Thread carregarObjetos() {
         Thread motor = new Thread(new Runnable() {
 
             public void run() {
                 List<T> lista;
                 if (capturarErros) {
                     lista = tentarGetCarregarObjetos();
                 } else {
                     lista = getCarregarObjetos();
                 }
                 setObjetos(lista, true, filtrarAposCarregar);
                 aposCarregar(lista);
             }
         }, "Carregar Objetos Modelo");
         motor.start();
 
         return motor;
     }
 
     protected void aposCarregar(List<T> objetosCarregados) {
     }
 
     final public Class<T> getClasse() {
         Type tipo = this.getClass().getGenericSuperclass();
         if (tipo instanceof Class) {
             Class classe = (Class) tipo;
             return classe;
         } else if (tipo instanceof ParameterizedType) {
             ParameterizedType tipoParametrizado = (ParameterizedType) tipo;
             return (Class<T>) tipoParametrizado.getActualTypeArguments()[0];
         }
         return (Class<T>) Object.class;
     }
 
     public abstract Component getComponente();
 
     public void acao(T objeto) {
         editar(objeto);
     }
 
     public void acao(List<T> objetos) {
         for (T o : objetos) {
             acao(o);
         }
     }
 
     final public void acao() {
         acao(getObjetosSelecionados());
     }
 
     final protected T tentarGetAdicionar() {
         try {
             return getAdicionar();
         } catch (Throwable t) {
             tratarExcecao(t);
             return null;
         }
     }
 
     protected T getAdicionar() {
         return null;
     }
 
     protected void aposAdicionar(T objetoAdicionado) {
     }
 
     final public void adicionar() {
         T objeto = null;
         if (capturarErros) {
             objeto = tentarGetAdicionar();
         } else {
             objeto = getAdicionar();
         }
         if (objeto == null) {
             return;
         }
         adicionar(objeto);
     }
 
     final protected void tentarEditar(T objeto) {
         try {
             editar(objeto);
         } catch (Throwable t) {
             tratarExcecao(t);
         }
     }
 
     public void editar(T objeto) {
     }
 
     final public void editar() {
         List<T> objetosSelecionados = getObjetosSelecionados(getLinhasSelecionadas());
         for (T objeto : objetosSelecionados) {
             if (objeto == null) {
                 return;
             }
             if (capturarErros) {
                 tentarEditar(objeto);
             } else {
                 editar(objeto);
             }
         }
     }
 
     protected boolean tentarGetRemover(T objeto) {
         try {
             return getRemover(objeto);
         } catch (Throwable t) {
             tratarExcecao(t);
             return false;
         }
     }
 
     protected boolean getRemover(T objeto) {
         return false;
     }
 
     protected void aposRemover(T objetoRemovido) {
     }
 
     final public void remover() {
         List<T> objetosSelecionados = getObjetosSelecionados(getLinhasSelecionadas());
         for (T o : objetosSelecionados) {
             boolean removeu;
             if (capturarErros) {
                 removeu = tentarGetRemover(o);
             } else {
                 removeu = getRemover(o);
             }
             if (!removeu) {
                 continue;
             }
             remover(o);
 
         }
     }
 
     final public void selecionar() {
         T novoObjetoSelecionado = getObjetoSelecionado();
         if (objetoSelecionado != novoObjetoSelecionado) {
             if (capturarErros) {
                 tentarSelecionar(novoObjetoSelecionado, objetoSelecionado);
             } else {
                 selecionar(novoObjetoSelecionado, objetoSelecionado);
             }
             objetoSelecionado = novoObjetoSelecionado;
         }
     }
 
     final protected void tentarSelecionar(T atual, T antigo) {
         try {
             selecionar(atual, antigo);
         } catch (Throwable t) {
             tratarExcecao(t);
         }
     }
 
     public void selecionar(T atual, T antigo) {
     }
 
     void setObjetos(T... objetos) {
         ArrayList<T> lista = new ArrayList<T>(objetos.length);
         Collections.addAll(lista, objetos);
         setObjetos(lista);
     }
 
     final public void setObjetos(List<T> lista) {
         setObjetos(lista, ordenada);
     }
 
     final public void setObjetos(List<T> lista, boolean ordenar) {
         setObjetos(lista, ordenar, true);
     }
 
     final public void setObjetos(List<T> lista, boolean ordenar, boolean filtrar) {
         if (lista == null) {
             lista = new ArrayList<T>();
             ordenar = false;
         }
         this.objetos = lista;
         if (ordenar) {
             ordenar(false, false);
         }
         if (filtrar) {
             filtrar(true);
         } else {
             notificar();
         }
     }
 
     final public boolean isOrdenada() {
         return ordenada;
     }
 
     final public void setOrdenada(boolean ordenada) {
         setOrdenada(ordenada, true);
     }
 
     final public void setOrdenada(boolean ordenada, boolean ordenar) {
         setOrdenada(ordenada, ordenar, true, true);
     }
 
     final public void setOrdenada(boolean ordenada, boolean ordenar, boolean filtrar, boolean notificar) {
         this.ordenada = ordenada;
         if (ordenada && ordenar) {
             ordenar(filtrar, notificar);
         }
     }
 
     final public boolean ordenar() {
         return ordenar(true, true);
     }
 
     final public boolean ordenar(boolean filtrar, boolean notificar) {
         if (objetos.contains(null) || objetos.size() == 0 ||
                 (!(objetos.get(0) instanceof Comparable) && comparador == null)) {
             if (notificar) {
                 notificar();
             }
             return false;
         }
 
         if (comparador == null) {
             try {
                 Collections.sort((List<Comparable>) objetos);
             } catch (ClassCastException ex) {
                 if (notificar) {
                     notificar();
                 }
                 return false;
             }
         } else {
             Collections.sort(objetos, comparador);
         }
 
         if (filtrar) {
             filtrar(notificar);
         } else if (notificar) {
             notificar();
         }
 
         return true;
     }
 
     final public int size() {
         return getObjetosFiltrados().size();
     }
 
     final public void incrementar(T... objetos) {
         for (T o : objetos) {
             adicionar(o);
         }
     }
 
     final public void incrementar(List<T> lista) {
         for (T o : lista) {
             adicionar(o);
         }
     }
 
     final public void adicionar(T objeto) {
         adicionar(objeto, objetos.size());
     }
 
     final public void adicionar(T objeto, int indice) {
         if (objeto == null) {
             return;
         }
         if (indice < 0 || indice > size()) {
             indice = size();
         }
         objetos.add(indice, objeto);
         filtrar(false);
         if (ordenada) {
             ordenar(false, true);
         } else {
             int indiceEmObjetosFiltrados = objetosFiltrados.indexOf(objeto);
             if (indiceEmObjetosFiltrados >= 0) {
                 notificarAdicao(indiceEmObjetosFiltrados);
             }
         }
         aposAdicionar(objeto);
     }
 
     final public void set(T... objetos) {
         for (T o : objetos) {
             set(o);
         }
     }
 
     final public T set(T objeto) {
         return set(objeto, objetos.indexOf(objeto));
     }
 
     final public T set(T objeto, int indice) {
         if (indice < 0 || indice > objetos.size()) {
             adicionar(objeto);
             return null;
         }
         T objetoAntigo = objetos.set(indice, objeto);
         filtrar(true);
         return objetoAntigo;
     }
 
     final public boolean remover(T objeto) {
         return remover(objetos.indexOf(objeto)) != null ? true : false;
     }
 
     final public T remover(int indiceLinha) {
         if (indiceLinha < 0 || indiceLinha >= objetos.size()) {
             return null;
         }
         int indiceEmObjetosFiltrados = objetosFiltrados.indexOf(objetos.get(indiceLinha));
         T objeto = objetos.remove(indiceLinha);
         if (objeto != null) {
             removidos.add(objeto);
             if (indiceEmObjetosFiltrados >= 0) {
                 objetosFiltrados.remove(indiceEmObjetosFiltrados);
                 notificarRemocao(indiceEmObjetosFiltrados);
             }
         }
         aposRemover(objeto);
         return objeto;
     }
 
     final public List<T> getRemovidos() {
         return removidos;
     }
 
     final public List<T> getObjetos() {
         return objetos;
     }
 
     final public List<T> getObjetosFiltrados() {
         return objetosFiltrados;
     }
 
     final protected void setObjetosFiltrados(List<T> listaFiltrada, boolean notificar) {
         if (listaFiltrada == null) {
             listaFiltrada = new ArrayList<T>();
         }
         objetosFiltrados = listaFiltrada;
         if (notificar) {
             notificar();
         }
     }
 
     public T getObjetoSelecionado() {
         return getObjeto(getLinhaSelecionada());
     }
 
     final public List<T> getObjetosSelecionados() {
         return getObjetosSelecionados(getLinhasSelecionadas());
     }
 
     final public List<T> getObjetosSelecionados(int[] indices) {
         List<T> objetos = new ArrayList<T>(indices.length);
         for (int i : indices) {
             T o = getObjeto(i);
             if (o == null) {
                 continue;
             }
             objetos.add(o);
         }
         return objetos;
     }
 
     final public T getObjeto(int indiceLinha) {
         if (indiceLinha < 0 || indiceLinha >= size()) {
             return null;
         }
         return getObjetosFiltrados().get(indiceLinha);
     }
 
     final public T getObjeto(Object objeto) {
         return getObjeto(getIndice(objeto));
     }
 
     final public int getIndice(Object objeto) {
         return getObjetosFiltrados().indexOf(objeto);
     }
 
     final public void notificar(T objeto) {
         notificar(getIndice(objeto));
     }
 
     final public boolean contains(Object objeto) {
         return getObjetosFiltrados().contains(objeto);
     }
 
     final public void limpar() {
         objetos.clear();
         objetosFiltrados.clear();
         removidos.clear();
         notificar();
     }
 
     final protected void tratarExcecao(Throwable t) {
         System.out.println("Excecão capturada pelo Modelo: " + t.getMessage());
         t.printStackTrace();
     //new TratadorExcecao(t, ApresentacaoUtil.getContainerPai(getComponente()), isExibirMensagemErro());
     }
 
     final protected void adicionarListener(Class classe, EventListener l) {
         listeners.add(classe, l);
     }
 
     final protected void removerListener(Class classe, EventListener l) {
         listeners.remove(classe, l);
     }
 
     protected class ControladorSelecaoLista implements ListSelectionListener {
 
         public void valueChanged(ListSelectionEvent e) {
             selecionar();
         }
     }
 
     private class ControladorSelecaoItem implements ItemListener {
 
         public void itemStateChanged(ItemEvent e) {
             selecionar();
         }
     }
 
     private class ControladorAcao implements KeyListener, MouseListener {
 
         public void keyPressed(KeyEvent e) {
             if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                 acao();
             }
             if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                 remover();
             }
         }
 
         public void mouseClicked(MouseEvent e) {
             if (e.getClickCount() >= 2) {
                 acao();
             }
         }
 
         public void keyTyped(KeyEvent e) {
         }
 
         public void keyReleased(KeyEvent e) {
         }
 
         public void mousePressed(MouseEvent e) {
         }
 
         public void mouseReleased(MouseEvent e) {
         }
 
         public void mouseEntered(MouseEvent e) {
         }
 
         public void mouseExited(MouseEvent e) {
         }
     }
 
     private class ListenerAcao implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             acao();
         }
     }
 
     private class ListenerAdicao implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             adicionar();
         }
     }
 
     private class ListenerEdicao implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             editar();
         }
     }
 
     private class ListenerRemocao implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             remover();
         }
     }
}
