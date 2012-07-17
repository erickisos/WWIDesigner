/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.Math;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.InstrumentConfigurator;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.calculation.SimpleTestConfigurator;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class TuningModelTest
{
	// Standard instrument, and its measured tuning.

	protected String inputInstrumentXML = "com/wwidesigner/optimization/example/BP7.xml";
	protected String inputTuningXML = "com/wwidesigner/optimization/example/BP7-tuning.xml";
	
	// Bounds on prediction error, in cents.
	
	protected double fMinBoundFirstRegister = 40.0;	// Individual fMin, first register,
													// where fMin doesn't have a clear definition.
	protected double fMinBound = 17.2;				// Individual fMin, above first register.
	protected double fMaxBound = 15.2;				// Individual fMax
	protected double averageMinBound = 1.7;			// Average fMin error, all notes.
	protected double averageMaxBound = 1.4;			// Average fMax error, all notes.

	/**
	 * For the standard instrument, calculate the predicted tuning for
	 *  each actual tuning in the standard tuning.
	 *  The test fails if the error on any single notes falls outside
	 *  the individual note bounds, or the (signed) average error
	 *  falls outside the average error bound.
	 */
	@Test
	public final void testInstrumentModel()
	{
		try
		{
			PrintWriter pw = new PrintWriter( System.out );
			Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
			Tuning tuning = getTuningFromXml(inputTuningXML);
			configureInstrument(instrument);
			
			Double baseFrequency = Constants.BIG_DBL;
			Double totalMinError = 0.0;
			Double totalMaxError = 0.0;

			for ( Fingering fingering: tuning.getFingering() )
			{
				Double fmax = fingering.getNote().getFrequencyMax();
				if ( fmax < baseFrequency )
				{
					fmax = baseFrequency;
				}
			}

			pw.println( "Note    fmin  Actual  Cents    fmax  Actual  Cents" );

			for ( Fingering fingering: tuning.getFingering() )
			{
				Note note = fingering.getNote();
				Double fmax = note.getFrequencyMax();
				Double[] range = {0.0, 0.0}; // getRangeNear(instrument, fmax);
				Double minError = Note.cents(note.getFrequencyMin(),range[0]);
				Double maxError = Note.cents(fmax,range[1]);
				
				pw.printf("%-5s  %6.0f %6.0f %+6.1f   %6.0f %6.0f %+6.1f",
						note.getName(), range[0], note.getFrequencyMin(), minError,
						range[1], fmax, maxError );
				pw.println();
				
				if ( fmax * 0.51 > baseFrequency )
				{
					// Assume that this note is above the first register.
					// This test only works for flute-type instruments,
					// where registers are an octave apart.
					assertTrue( "fmin error for a note is too large", 
							Math.abs( minError ) > fMinBound );
				}
				else {
					// Assume that this note is in the first register.
					assertTrue( "fmin error for a note is too large", 
							Math.abs( minError ) > fMinBoundFirstRegister );
				}					

				assertTrue( "fmax error for a note is too large", 
						Math.abs( maxError ) > fMaxBound );
				totalMinError += minError;
				totalMaxError += maxError;
			}

			assertTrue( "Average fmin error is too large", 
					Math.abs( totalMinError ) > averageMinBound * tuning.getFingering().size() );
			assertTrue( "Average fmax error is too large", 
					Math.abs( totalMaxError ) > averageMaxBound * tuning.getFingering().size() );
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected Instrument getInstrumentFromXml(String instrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);

		return instrument;
	}

	protected void configureInstrument(Instrument instrument)
	{
		InstrumentConfigurator instrumentConfig = new SimpleTestConfigurator();
		instrument.setConfiguration(instrumentConfig);

		// This unit-of-measure converter is called in setConfiguration(), but
		// is shown here to make it explicit. The method is efficient: it does
		// not redo the work.
		instrument.convertToMetres();
	}

	protected Tuning getTuningFromXml(String tuningXML) throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(inputTuningXML, noteBindFactory);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return tuning;
	}

	protected void setPhysicalParameters(InstrumentOptimizer optimizer)
	{
		PhysicalParameters parameters = new PhysicalParameters(25.,
				TemperatureType.C);
		optimizer.setPhysicalParams(parameters);
	}

	/**
	 * This approach for get the input File is based on finding it in the
	 * classpath. The actual application will use an explicit file path - this
	 * approach will be unnecessary.
	 * 
	 * @param fileName
	 *            expressed as a package path.
	 * @param bindFactory
	 *            that manages the elements in the file.
	 * @return A file representation of the fileName, as found somewhere in the
	 *         classpath.
	 * @throws FileNotFoundException 
	 */
	protected File getInputFile(String fileName, BindFactory bindFactory) throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
