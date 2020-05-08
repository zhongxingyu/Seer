 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able to
  * manipulate and create vector and raster spatial information. OrbisGIS is
  * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
  *
  * 
  *  Team leader Erwan BOCHER, scientific researcher,
  * 
  *  User support leader : Gwendall Petit, geomatic engineer.
  *
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT, Adelin PIAU
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://www.orbisgis.org/>
  *
  * or contact directly:
  * info _at_ orbisgis.org
  */
 package org.gdms.gdmstopology.function;
 
 import com.vividsolutions.jts.geom.Geometry;
 import java.util.Collection;
 import java.util.List;
 
 import org.gdms.data.SQLDataSourceFactory;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DriverException;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.driverManager.DriverLoadException;
 import org.gdms.driver.generic.GenericObjectDriver;
 import org.gdms.gdmstopology.process.LineNoder;
 import org.gdms.sql.function.FunctionSignature;
 import org.orbisgis.progress.ProgressMonitor;
 
 import org.gdms.sql.function.FunctionException;
 import org.gdms.sql.function.ScalarArgument;
 import org.gdms.sql.function.table.AbstractTableFunction;
 import org.gdms.sql.function.table.TableDefinition;
 import org.gdms.sql.function.table.TableFunctionSignature;
 
 public class ST_ToLineNoder extends AbstractTableFunction {
         
 	@SuppressWarnings( { "unchecked", "static-access" })
         @Override
 	public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, 
                                 Value[] values, ProgressMonitor pm) throws FunctionException {
 		try {
			final DataSet inSds = tables[0];
 			final LineNoder lineNoder = new LineNoder(inSds);
 
 			final Collection lines = lineNoder.getLines();
 			final Geometry nodedGeom = lineNoder.getNodeLines((List) lines);
 			final Collection<Geometry> nodedLines = lineNoder
 					.toLines(nodedGeom);
 
 			// build and populate the resulting driver
 			final GenericObjectDriver driver = new GenericObjectDriver(
 					getMetadata(null));
 
 			int k = 0;
 			final int rowCount = nodedLines.size();
 			for (Geometry geometry : nodedLines) {
 				if (k / 100 == k / 100.0) {
 					if (pm.isCancelled()) {
 						break;
 					} else {
 						pm.progressTo((int) (100 * k / rowCount));
 					}
 				}
 
 				driver.addValues(new Value[] { ValueFactory.createValue(k),
 						ValueFactory.createValue(geometry) });
 				k++;
 			}
 			return driver;
 		} catch (DriverException e) {
 			throw new FunctionException(e);
 		} catch (DriverLoadException e) {
 			throw new FunctionException(e);
 		}
 	}
 
         @Override
 	public String getDescription() {
 		return "Build all intersection and convert the geometries into lines ";
 	}
 
         @Override
 	public String getSqlOrder() {
 		return "select ST_ToLineNoder(the_geom) from myTable;";
 	}
 
         @Override
 	public String getName() {
 		return "ST_ToLineNoder";
 	}
 
         @Override
 	public Metadata getMetadata(Metadata[] tables) throws DriverException {
 		return new DefaultMetadata(new Type[] {
 				TypeFactory.createType(Type.INT),
 				TypeFactory.createType(Type.GEOMETRY) }, new String[] { "gid",
 				"the_geom" });
 	}
 
 	public TableDefinition[] getTablesDefinitions() {
 		return new TableDefinition[] { TableDefinition.GEOMETRY };
 	}
 
 
         @Override
         public FunctionSignature[] getFunctionSignatures() {
                 return new FunctionSignature[]{new TableFunctionSignature(TableDefinition.GEOMETRY, ScalarArgument.GEOMETRY)};
         }
 }
