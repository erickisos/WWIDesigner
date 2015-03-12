/**
 * 
 */
package com.wwidesigner.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.DialogResponse;
import com.jidesoft.app.framework.gui.GUIApplication;
import com.jidesoft.app.framework.gui.PreferencesPane;
import com.wwidesigner.gui.util.DirectoryChooserPanel;
import com.wwidesigner.util.view.LengthTypeComboBox;

/**
 * Dialog pane to display/modify preferences for the optimization application.
 * 
 * @author Burton Patkau
 *
 */
public class OptimizationPreferences extends PreferencesPane
{
	public static final String STUDY_MODEL_OPT = "StudyModel";
	public static final String NAF_STUDY_NAME = "NafStudy";
	public static final String WHISTLE_STUDY_NAME = "WhistleStudy";

	public static final String BLOWING_LEVEL_OPT = "BlowingLevel";
	public static final int DEFAULT_BLOWING_LEVEL = 5;

	public static final String TEMPERATURE_OPT = "AirTemperature";
	public static final String PRESSURE_OPT = "AirPressure";
	public static final String HUMIDITY_OPT = "RelHumidity";
	public static final String CO2_FRACTION_OPT = "CO2Fraction";
	public static final double DEFAULT_TEMPERATURE = 20;
	public static final double DEFAULT_PRESSURE = 101.325;
	public static final int DEFAULT_HUMIDITY = 45;
	public static final int DEFAULT_CO2_FRACTION = 390;

	public static final String CONSTRAINTS_DIRECTORY = "Constraints directory";
	public static final String DEFAULT_CONSTRAINTS_DIRECTORY = "";

	public static final int NrOptimizers = 6; // Including default
	public static final String OPTIMIZER_TYPE_OPT = "OptimizerType";
	public static final String OPT_DEFAULT_NAME = "Default";
	public static final String OPT_BOBYQA_NAME = "BOBYQA";
	public static final String OPT_CMAES_NAME = "CMAES";
	public static final String OPT_MULTISTART_NAME = "Multi-Start";
	public static final String OPT_SIMPLEX_NAME = "Simplex";
	public static final String OPT_POWELL_NAME = "Powell";

	public static final String LENGTH_TYPE_OPT = "Length Type";
	public static final String LENGTH_TYPE_DEFAULT = "IN";

	protected static String[] OptimizerName = new String[] { OPT_DEFAULT_NAME,
			OPT_BOBYQA_NAME, OPT_CMAES_NAME, OPT_MULTISTART_NAME,
			OPT_SIMPLEX_NAME, OPT_POWELL_NAME };

	JRadioButton nafButton;
	JRadioButton whistleButton;
	ButtonGroup studyGroup;

	JRadioButton[] optButton = new JRadioButton[NrOptimizers];
	ButtonGroup optimizerGroup;

	JSpinner blowingLevelSpinner;
	SpinnerNumberModel blowingLevel;

	NumberFormat floatFormat;

	JFormattedTextField temperatureField;
	JFormattedTextField pressureField;
	JFormattedTextField humidityField;
	JFormattedTextField co2Field;
	JFormattedTextField topHoleRatioField;
	DirectoryChooserPanel constraintsDirChooser;

	LengthTypeComboBox lengthTypeComboBox;

	JTextField generalMessageField;
	JTextField nafMessageField;
	JTextField whistleMessageField;

	OptionsTabView optionsPane;

	@Override
	protected void initializeComponents(DialogRequest request)
	{
		// Setup dialog components.

		nafButton = new JRadioButton("NAF Study");
		whistleButton = new JRadioButton("Whistle Study");
		studyGroup = new ButtonGroup();
		studyGroup.add(nafButton);
		studyGroup.add(whistleButton);

		blowingLevel = new SpinnerNumberModel(DEFAULT_BLOWING_LEVEL, 0, 10, 1);
		blowingLevelSpinner = new JSpinner(blowingLevel);
		blowingLevelSpinner.setName("Blowing Level");
		lengthTypeComboBox = new LengthTypeComboBox();

		floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(1);
		temperatureField = new JFormattedTextField(floatFormat);
		pressureField = new JFormattedTextField(floatFormat);
		humidityField = new JFormattedTextField();
		co2Field = new JFormattedTextField();
		temperatureField.setColumns(5);
		pressureField.setColumns(5);
		humidityField.setColumns(5);
		co2Field.setColumns(5);

		constraintsDirChooser = new DirectoryChooserPanel();

		generalMessageField = new JTextField(22);
		generalMessageField.setEditable(false);
		generalMessageField.setText("");
		nafMessageField = new JTextField(35);
		nafMessageField.setEditable(false);
		nafMessageField.setText("");
		whistleMessageField = new JTextField(25);
		whistleMessageField.setEditable(false);
		whistleMessageField.setText("");

		optimizerGroup = new ButtonGroup();
		for (int i = 0; i < NrOptimizers; ++i)
		{
			optButton[i] = new JRadioButton(OptimizerName[i]);
			optimizerGroup.add(optButton[i]);
		}
		optButton[0].setSelected(true);

		optionsPane = new OptionsTabView();
		add(optionsPane);

		setDefaultStudy();
	}

	@Override
	protected void updateComponents(DialogRequest request)
	{
		// Set or re-set the value state of components.
		Preferences myPreferences = getApplication().getPreferences();
		String modelName = myPreferences.get(STUDY_MODEL_OPT, NAF_STUDY_NAME);
		if (modelName.contentEquals(NAF_STUDY_NAME))
		{
			nafButton.setSelected(true);
			whistleButton.setSelected(false);
		}
		else
		{
			whistleButton.setSelected(true);
			nafButton.setSelected(false);
		}
		Number currentLevel = myPreferences.getInt(BLOWING_LEVEL_OPT,
				DEFAULT_BLOWING_LEVEL);
		blowingLevel.setValue(currentLevel);
		Number currentTemperature = myPreferences.getDouble(TEMPERATURE_OPT,
				DEFAULT_TEMPERATURE);
		temperatureField.setValue(currentTemperature);
		Number currentPressure = myPreferences.getDouble(PRESSURE_OPT,
				DEFAULT_PRESSURE);
		pressureField.setValue(currentPressure);
		Number currentHumidity = myPreferences.getInt(HUMIDITY_OPT,
				DEFAULT_HUMIDITY);
		humidityField.setValue(currentHumidity);
		Number currentCO2 = myPreferences.getInt(CO2_FRACTION_OPT,
				DEFAULT_CO2_FRACTION);
		co2Field.setValue(currentCO2);

		String optimizerType = myPreferences.get(OPTIMIZER_TYPE_OPT,
				OPT_DEFAULT_NAME);

		// Ensure first button, Default, is selected if optimizer name not
		// found.
		optButton[0].setSelected(true);
		for (int i = 1; i < NrOptimizers; ++i)
		{
			if (optimizerType.contentEquals(OptimizerName[i]))
			{
				optButton[0].setSelected(false);
				optButton[i].setSelected(true);
			}
			else
			{
				optButton[i].setSelected(false);
			}
		}

		String constraintsPath = myPreferences.get(CONSTRAINTS_DIRECTORY,
				DEFAULT_CONSTRAINTS_DIRECTORY);
		constraintsDirChooser.setSelectedDirectory(constraintsPath);

		String dimensionType = myPreferences.get(LENGTH_TYPE_OPT,
				LENGTH_TYPE_DEFAULT);
		lengthTypeComboBox.setSelectedLengthType(dimensionType);
	}

	@Override
	protected void updatePreferences(GUIApplication application)
	{
		// Update the application preferences, from the dialog values.
		Preferences myPreferences = application.getPreferences();
		String priorStudyName = myPreferences.get(STUDY_MODEL_OPT, "");
		String studyName;
		String optimizerName = OPT_DEFAULT_NAME;

		if (nafButton.isSelected())
		{
			studyName = NAF_STUDY_NAME;
		}
		else
		{
			studyName = WHISTLE_STUDY_NAME;
		}

		for (int i = 0; i < NrOptimizers; ++i)
		{
			if (optButton[i].isSelected())
			{
				optimizerName = OptimizerName[i];
			}
		}

		// Update the preferences, and re-set the view's study model.

		myPreferences.put(STUDY_MODEL_OPT, studyName);
		myPreferences.putInt(BLOWING_LEVEL_OPT, blowingLevel.getNumber()
				.intValue());
		myPreferences.putDouble(TEMPERATURE_OPT,
				((Number) temperatureField.getValue()).doubleValue());
		myPreferences.putDouble(PRESSURE_OPT,
				((Number) pressureField.getValue()).doubleValue());
		myPreferences.putInt(HUMIDITY_OPT,
				((Number) humidityField.getValue()).intValue());
		myPreferences.putInt(CO2_FRACTION_OPT,
				((Number) co2Field.getValue()).intValue());
		myPreferences.put(OPTIMIZER_TYPE_OPT, optimizerName);

		myPreferences.put(CONSTRAINTS_DIRECTORY,
				constraintsDirChooser.getSelectedDirectory());

		String dimensionType = lengthTypeComboBox.getSelectedLengthTypeName();
		myPreferences.put(LENGTH_TYPE_OPT, dimensionType);

		DataView[] views = application.getFocusedViews();
		for (DataView view : views)
		{
			if (view instanceof StudyView)
			{
				StudyView studyView = (StudyView) view;
				if (!studyName.contentEquals(priorStudyName))
				{
					// Study model name has changed.
					studyView.setStudyModel(studyName);
				}
				studyView.getStudyModel().setPreferences(myPreferences);
				// Because some of these preference changes might impact Actions
				// in the UI ...
				studyView.updateActions();
			}
		}
	}

	@Override
	protected void validateComponents(DialogRequest request,
			DialogResponse response) throws ApplicationVetoException
	{
		// Validate the preferences.
		generalMessageField.setText("");
		generalMessageField.setForeground(Color.BLACK);
		nafMessageField.setText("");
		nafMessageField.setForeground(Color.BLACK);
		whistleMessageField.setText("");
		whistleMessageField.setForeground(Color.BLACK);

		double currentTemperature = ((Number) temperatureField.getValue())
				.doubleValue();
		if (currentTemperature < 0 || currentTemperature > 50)
		{
			generalMessageField
					.setText("Temperature must be between 0 and 50 C.");
			generalMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		double currentPressure = ((Number) pressureField.getValue())
				.doubleValue();
		if (currentPressure < 10 || currentPressure > 150)
		{
			whistleMessageField
					.setText("Pressure must be between 10 and 150 kPa.");
			whistleMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		int currentHumidity = ((Number) humidityField.getValue()).intValue();
		if (currentHumidity < 0 || currentHumidity > 100)
		{
			generalMessageField.setText("Humidity must be between 0 and 100%.");
			generalMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		int currentCO2 = ((Number) co2Field.getValue()).intValue();
		if (currentCO2 < 0 || currentCO2 > 100000)
		{
			whistleMessageField
					.setText("CO2 must be between 0 and 100,000 ppm.");
			whistleMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		if (nafButton.isSelected())
		{
			String constraintsPath = constraintsDirChooser
					.getSelectedDirectory();
			// Allow an empty or blank path
			if (constraintsPath != null && constraintsPath.trim().length() > 0)
			{
				try
				{
					File directory = new File(constraintsPath);
					if (!directory.exists() || !directory.isDirectory()
							|| !directory.canRead() || !directory.canWrite())
					{
						throw new Exception();
					}
				}
				catch (Exception e)
				{
					nafMessageField
							.setText("Constraints directory location is not valid");
					nafMessageField.setForeground(Color.RED);
					throw new ApplicationVetoException();
				}
			}
		}
	}

	/**
	 * Convenience method so that the application can be distributed with any of
	 * the studies as the initial default.
	 */
	protected void setDefaultStudy()
	{
		nafButton.setSelected(true);
	}

	class OptionsTabView extends JTabbedPane
	{
		OptionsTabView()
		{
			add(new StudyPanel(), "General Study Options");
			add(new NafPanel(), "NAF Only Options");
			add(new WhistlePanel(), "Whistle Only Options");
		}
	}

	class StudyPanel extends JPanel
	{
		StudyPanel()
		{
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(nafButton, gbc);
			gbc.gridx = 1;
			add(whistleButton, gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			add(new JLabel(LENGTH_TYPE_OPT + ": "), gbc);
			gbc.gridx = 1;
			add(lengthTypeComboBox, gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			add(new JLabel("Temperature, C: "), gbc);
			gbc.gridx = 1;
			add(temperatureField, gbc);
			gbc.gridx = 0;
			gbc.gridy = 3;
			add(new JLabel("Relative Humidity, %: "), gbc);
			gbc.gridx = 1;
			add(humidityField, gbc);
			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.insets = new Insets(10, 0, 5, 0);
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.CENTER;
			add(generalMessageField, gbc);
		}
	}

	class NafPanel extends JPanel
	{
		NafPanel()
		{
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel(CONSTRAINTS_DIRECTORY + ": "), gbc);
			gbc.gridy = 1;
			add(constraintsDirChooser, gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = new Insets(10, 0, 5, 0);
			gbc.anchor = GridBagConstraints.CENTER;
			add(nafMessageField, gbc);
		}
	}

	class WhistlePanel extends JPanel
	{
		WhistlePanel()
		{
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(5, 0, 3, 0);
			add(new JLabel("Blowing Level: "), gbc);
			gbc.gridx = 1;
			add(blowingLevelSpinner, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(new JLabel("Pressure, kPa: "), gbc);
			gbc.gridx = 1;
			add(pressureField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			add(new JLabel("CO2, ppm: "), gbc);
			gbc.gridx = 1;
			add(co2Field, gbc);

			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.insets = new Insets(0, 10, 0, 0);
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.gridwidth = 2;
			add(new JLabel("Optimizer Type: "), gbc);

			JPanel optimizerPanel1 = new JPanel();
			optimizerPanel1.setLayout(new BoxLayout(optimizerPanel1,
					BoxLayout.Y_AXIS));
			// optimizerPanel1.add(new JLabel("Optimizer Type:"));
			// Add all but Powell optimizer to the optimizerPanel.
			for (int i = 0; i < 3; ++i)
			{
				optimizerPanel1.add(optButton[i]);
			}
			gbc.gridx = 2;
			gbc.gridy = 1;
			gbc.gridheight = 3;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			// gbc.insets = new Insets(0, 10, 0, 0);
			add(optimizerPanel1, gbc);

			JPanel optimizerPanel2 = new JPanel();
			optimizerPanel2.setLayout(new BoxLayout(optimizerPanel2,
					BoxLayout.Y_AXIS));
			// Add all but Powell optimizer to the optimizerPanel.
			for (int i = 3; i < NrOptimizers - 1; ++i)
			{
				optimizerPanel2.add(optButton[i]);
			}
			gbc.gridx = 3;
			gbc.gridy = 1;
			gbc.gridheight = 2;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(optimizerPanel2, gbc);

			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.insets = new Insets(10, 0, 5, 0);
			gbc.gridwidth = 4;
			gbc.anchor = GridBagConstraints.CENTER;
			add(whistleMessageField, gbc);
		}
	}

}
