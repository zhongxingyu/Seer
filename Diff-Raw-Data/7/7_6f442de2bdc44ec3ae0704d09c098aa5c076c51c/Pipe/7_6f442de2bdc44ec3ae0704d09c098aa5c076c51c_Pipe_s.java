 package pleocmd.pipe;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import pleocmd.Log;
 import pleocmd.exc.ConverterException;
 import pleocmd.exc.InputException;
 import pleocmd.exc.OutputException;
 import pleocmd.exc.PipeException;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 /**
  * @author oliver
  */
 public final class Pipe extends StateHandling {
 
 	private final List<Input> inputList = new ArrayList<Input>();
 
 	private final List<Output> outputList = new ArrayList<Output>();
 
 	private final List<Converter> converterList = new ArrayList<Converter>();
 
 	private final Set<Converter> ignoredConverter = new HashSet<Converter>();
 
 	private final Set<Output> ignoredOutputs = new HashSet<Output>();
 
 	private int inputPosition;
 
 	public Pipe() {
 		try {
 			setState(State.Constructed);
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 	}
 
 	public List<Input> getInputList() {
 		return Collections.unmodifiableList(inputList);
 	}
 
 	public List<Output> getOutputList() {
 		return Collections.unmodifiableList(outputList);
 	}
 
 	public List<Converter> getConverterList() {
 		return Collections.unmodifiableList(converterList);
 	}
 
 	public void addInput(final Input input) {
 		// TODO add ensure...
 		inputList.add(input);
 	}
 
 	public void addConverter(final Converter converter) {
 		// TODO add ensure...
 		converterList.add(converter);
 	}
 
 	public void addOutput(final Output output) {
 		// TODO add ensure...
 		outputList.add(output);
 	}
 
 	private Data getFromInput() {
 		Log.detail("Reading one data block from input");
 		Input in;
 		while (true) {
 			assert inputPosition <= inputList.size();
 			if (inputPosition >= inputList.size()) {
 				Log.detail("InputList is empty");
 				return null;
 			}
 			in = inputList.get(inputPosition);
 			try {
 				if (in.canReadData()) {
 					final Data res = in.readData();
 					if (res == null)
 						throw new InputException(in, false,
 								"readData() returned null");
 					return res;
 				}
 			} catch (final InputException e) {
 				Log.error(e);
 				if (e.isPermanent()) {
 					Log.detail("Skipping no longer working input '%s'", in);
 					in.tryClose();
 					++inputPosition;
 				} else
 					Log.detail("Skipping one data block from input '%s'", in);
 				// try next data packet / try from next input
 				continue;
 			}
 			// no more data available in this Input, so
 			// switch to the next one
 			Log.detail("Switching to next input");
 			in.tryClose();
 			++inputPosition;
 			// try data packet from next input
 		}
 	}
 
 	/**
 	 * Converts one {@link Data} object to a list of {@link Data} objects if a
 	 * fitting {@link Converter} could be found. Otherwise the {@link Data}
 	 * object itself is returned.
 	 * 
 	 * @param data
 	 *            The {@link Data} object to be converted.
 	 * @return A single-element list holding the data object given by
 	 *         <b>data</b> if no fitting {@link Converter} could be found or a
 	 *         list of new {@link Data} objects returned by the first fitting
 	 *         {@link Converter}.
 	 */
 	private List<Data> convertDataToDataList(final Data data) {
 		Log.detail("Converting data block to list of data blocks");
 		List<Data> res = null;
 		for (final Converter cvt : converterList)
 			if (!ignoredConverter.contains(cvt)
 					&& (res = convertOneData(data, cvt)) != null) return res;
 		// CS_IGNORE_PREV need inner assignment
 
 		// no fitting (and not ignored and working) converter found
 		res = new ArrayList<Data>(1);
 		res.add(data);
 		return res;
 	}
 
 	private List<Data> convertOneData(final Data data, final Converter cvt) {
 		try {
 			if (cvt.canHandleData(data)) {
 				Log.detail("Converting with '%s'", cvt);
 				final List<Data> newDatas = cvt.convert(data);
 				final List<Data> res = new ArrayList<Data>(newDatas.size());
 				for (final Data newData : newDatas)
 					res.addAll(convertDataToDataList(newData));
 				return res;
 			}
 		} catch (final ConverterException e) {
 			Log.error(e);
 			if (e.isPermanent()) {
 				Log.detail("Removing no longer working converter '%s'", cvt);
 				cvt.tryClose();
 				ignoredConverter.add(cvt);
 			} else
 				Log.detail("Skipping converter '%s' for one data block '%s'",
 						cvt, data);
 		}
 		return null;
 	}
 
 	private void writeAllData(final List<Data> list) {
 		Log.detail("Writing %d data block(s) to %d output(s)", list.size(),
 				outputList.size());
 		for (final Data data : list)
 			for (final Output out : outputList)
 				if (!ignoredOutputs.contains(out)) writeToOutput(data, out);
 	}
 
 	private void writeToOutput(final Data data, final Output out) {
 		try {
 			out.write(data);
 		} catch (final OutputException e) {
 			Log.error(e);
 			if (e.isPermanent()) {
 				Log.detail("Removing no longer working output '%s'", out);
 				out.tryClose();
 				ignoredOutputs.add(out);
 			} else
 				Log.detail("Skipping output '%s' for one data block '%s'", out,
 						data);
 		}
 	}
 
 	public boolean pipeData() throws PipeException {
 		ensureInitialized();
 		// read next data block ...
 		final Data data = getFromInput();
 		if (data == null) return false; // marks end of all inputs
 
 		// ... convert it ...
 		final List<Data> dataList = convertDataToDataList(data);
 
 		// ... and send all data blocks to all connected outputs
 		writeAllData(dataList);
 		return true;
 	}
 
 	public int pipeAllData() throws PipeException {
 		ensureInitialized();
 		int count = 0;
 		while (pipeData())
 			++count;
 		Log.detail("Piped %d data blocks", count);
 		return count;
 	}
 
 	public void configuredAll() throws PipeException {
 		ensureConstructed();
 		Log.detail("Marking all input as configured");
 		for (final Input in : inputList)
 			in.configured();
 		Log.detail("Marking all converter as configured");
 		for (final Converter cvt : converterList)
 			cvt.configured();
 		Log.detail("Marking all output as configured");
 		for (final Output out : outputList)
 			out.configured();
 		setState(State.Configured);
 	}
 
 	public void initializeAll() throws PipeException {
 		ensureConfigured();
 		Log.detail("Initializing all input");
 		for (final Input in : inputList)
 			in.init();
 		Log.detail("Initializing all converter");
 		for (final Converter cvt : converterList)
 			cvt.init();
 		Log.detail("Initializing all output");
 		for (final Output out : outputList)
 			out.init();
 		setState(State.Initialized);
 	}
 
 	public void closeAll() throws PipeException {
 		ensureInitialized();
 		Log.detail("Closing all input");
 		for (int i = inputPosition; i < inputList.size(); ++i)
 			inputList.get(i).tryClose();
 		Log.detail("Closing all converter");
 		for (final PipePart pp : converterList)
 			if (!ignoredConverter.contains(pp)) pp.tryClose();
 		Log.detail("Closing all output");
 		for (final PipePart pp : outputList)
 			if (!ignoredOutputs.contains(pp)) pp.tryClose();
 		setState(State.Configured);
 	}
 
 	public void reset() throws PipeException {
 		ensureNoLongerInitialized();
 		inputList.clear();
 		converterList.clear();
 		outputList.clear();
		inputPosition = 0;
		ignoredConverter.clear();
		ignoredOutputs.clear();
 	}
 
 	public void writeToFile(final File file) throws IOException, PipeException {
 		ensureNoLongerInitialized();
 		final Writer out = new FileWriter(file);
 		for (final PipePart pp : inputList) {
 			out.write(pp.getClass().getSimpleName());
 			out.write(":\n");
 			pp.getConfig().writeToFile(out);
 		}
 		for (final PipePart pp : converterList) {
 			out.write(pp.getClass().getSimpleName());
 			out.write(":\n");
 			pp.getConfig().writeToFile(out);
 		}
 		for (final PipePart pp : outputList) {
 			out.write(pp.getClass().getSimpleName());
 			out.write(":\n");
 			pp.getConfig().writeToFile(out);
 		}
 		out.close();
 	}
 
 	public boolean readFromFile(final File file) throws IOException,
 			PipeException {
 		ensureNoLongerInitialized();
 		boolean skipped = false;
 		reset();
 		final BufferedReader in = new BufferedReader(new FileReader(file));
 		while (true) {
 			String line = in.readLine();
 			if (line == null) break;
 			line = line.trim();
 			if (!line.endsWith(":"))
 				throw new IOException("Missing ':' at end of line");
 			final String cn = line.substring(0, line.length() - 1);
 			final String pckName = getClass().getPackage().getName();
 			String fcn;
 			PipePart pp;
 			try {
 				try {
 					fcn = String.format("%s.in.%s", pckName, cn);
 					pp = (PipePart) getClass().getClassLoader().loadClass(fcn)
 							.newInstance();
 				} catch (final ClassNotFoundException e1) {
 					try {
 						fcn = String.format("%s.cvt.%s", pckName, cn);
 						pp = (PipePart) getClass().getClassLoader().loadClass(
 								fcn).newInstance();
 					} catch (final ClassNotFoundException e2) {
 						try {
 							fcn = String.format("%s.out.%s", pckName, cn);
 							pp = (PipePart) getClass().getClassLoader()
 									.loadClass(fcn).newInstance();
 						} catch (final ClassNotFoundException e3) {
 							throw new PipeException(null, true,
 									"Cannot find PipePart with class-name '%s' in any "
 											+ "package under '%s'", cn, pckName);
 						}
 					}
 				}
 			} catch (final InstantiationException e) {
 				throw new PipeException(null, true, e,
 						"Cannot create PipePart of class '%s'", cn);
 			} catch (final IllegalAccessException e) {
 				throw new PipeException(null, true, e,
 						"Cannot create PipePart of class '%s'", cn);
 			}
 			try {
 				in.mark(0);
 				pp.getConfig().readFromFile(in);
 			} catch (final IOException e) {
 				// skip this pipe part and try to read the next one
 				Log.error(String.format("Skipped reading '%s' from file:", cn));
 				Log.error(e);
 				in.reset();
 				skipped = true;
 				continue;
 			}
 			if (pp instanceof Input)
 				inputList.add((Input) pp);
 			else if (pp instanceof Converter)
 				converterList.add((Converter) pp);
 			else if (pp instanceof Output)
 				outputList.add((Output) pp);
 			else
 				throw new PipeException(pp, true,
 						"Cannot create PipePart of class '%s': "
 								+ "Unknown super class", cn);
 		}
 		in.close();
 		return !skipped;
 	}
 }
