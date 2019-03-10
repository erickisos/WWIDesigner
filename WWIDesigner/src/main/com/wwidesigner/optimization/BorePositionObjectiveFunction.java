/**
 * Optimization objective function for relative position of existing bore points.
 * 
 * Copyright (C) 2016, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for relative position of existing bore points.
 * The optimization dimensions are:
 * <ul>
 * <li>Absolute position of bottom bore point.</li>
 * <li>For interior bore points down to the bottom,
 * distance from prior bore point to this bore point,
 * as a fraction of the distance from the prior bore point to the bottom.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The positions of bore points above these are left unchanged.
 * Bore point diameters are invariant. <br>
 * 
 * Do not use with other optimizers that might change the number of bore points.
 * Specify bottomPointUnchanged = true to use with optimizers that might change
 * the bottom bore position.
 * 
 * @author Burton Patkau
 * 
 */
public class BorePositionObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore point positions";
	public static final String DISPLAY_NAME = "Bore Position optimizer";
	// Number of invariant bore points at the top of the instrument.
	protected final int unchangedBorePoints;
	// Set to 1 if bottom bore point is left unchanged,
	// allowing other objective functions to manage it.
	protected final int unchangedBottomPoint;
	// Invariant: nrDimensions + unchangedBorePoints + unchangedBottomPoint = number of bore points.

	/**
	 * Create an optimization objective function for relative position of existing
	 * bore points, from the bottom of the bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints - Index of first bore point to optimize.
	 *        Leave position unchanged for this many bore points from the top of the bore.
	 * @param bottomPointUnchanged
	 */
	public BorePositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, int aUnchangedBorePoints,
			boolean bottomPointUnchanged)
	{
		super(aCalculator, tuning, aEvaluator);
		int nrBorePoints = aCalculator.getInstrument().getBorePoint().size();
		if (bottomPointUnchanged)
		{
			this.unchangedBottomPoint = 1;
		}
		else
		{
			this.unchangedBottomPoint = 0;
		}
		if (aUnchangedBorePoints >= 1)
		{
			this.unchangedBorePoints = aUnchangedBorePoints;
		}
		else
		{
			// At a minimum, top bore point is unchanged.
			this.unchangedBorePoints = 1;
		}
		nrDimensions = nrBorePoints - this.unchangedBorePoints - this.unchangedBottomPoint;
		if (nrDimensions > 1)
		{
			optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		}
		else
		{
			optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		}
		maxEvaluations = 10000;
		setConstraints();
	}

	/**
	 * Create an optimization objective function for relative position of existing
	 * bore points, from the bottom of the bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints - Index of first bore point to optimize.
	 *        Leave position unchanged for this many bore points from the top of the bore.
	 */
	public BorePositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, int aUnchangedBorePoints)
	{
		this(aCalculator, tuning, aEvaluator, aUnchangedBorePoints, false);
	}

	/**
	 * Create an optimization objective function for relative position of existing
	 * bore points, from the bottom of the bore.  The lowest bore point left
	 * unchanged will be the highest bore point with a name that contains "Body".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public BorePositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromBottomObjectiveFunction
						.getTopOfBody(aCalculator.getInstrument()) + 1, false);
	}

	protected void setConstraints()
	{
		int nrBorePoints = nrDimensions + unchangedBorePoints + unchangedBottomPoint;
		String name;
		int dimension = 0;
		int pointNr;
		if (unchangedBottomPoint == 0)
		{
			pointNr = borePointNr(0);
			name = "Position of bore point " + String.valueOf(pointNr)
					+ " (bottom)";
			constraints.addConstraint(new Constraint(CONSTR_CAT,
					name, ConstraintType.DIMENSIONAL));
			dimension = 1;
		}
		for (; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			name = "Relative position of bore point " + String.valueOf(pointNr)
					+ " between points " + String.valueOf(pointNr - 1)
					+ " and " + String.valueOf(nrBorePoints);
			constraints.addConstraint(new Constraint(CONSTR_CAT,
					name, ConstraintType.DIMENSIONLESS));
		}
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}
	
	/**
	 * Convert a dimension number in 0 .. nrDimensions-1
	 * to a bore point number in unchangedBorePoints+1 .. nrBorePoints.
	 */
	protected int borePointNr(int dimensionIdx)
	{
		if (dimensionIdx == 0 && unchangedBottomPoint == 0)
		{
			// First dimension is bottom bore point.
			return nrDimensions + unchangedBorePoints;
		}
		// Process remaining bore points in order, from top to bottom.
		return unchangedBorePoints + dimensionIdx + unchangedBottomPoint;
	}

	/**
	 * Point number used as an initial reference for the first changed point.
	 */
	protected int referencePointNr()
	{
		return unchangedBorePoints;
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		int dimension = 0;
		int pointNr;
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double lastBorePosition = borePoint.getBorePosition();

		if (unchangedBottomPoint == 0)
		{
			geometry[0] = borePoint.getBorePosition();
			dimension = 1;
		}

		borePoint = (BorePoint) sortedPoints[referencePointNr() - 1];
		double priorBorePosition = borePoint.getBorePosition();
		for (; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			borePoint = (BorePoint) sortedPoints[pointNr - 1];
			geometry[dimension] = (borePoint.getBorePosition() - priorBorePosition)
					/ (lastBorePosition - priorBorePosition);
			priorBorePosition = borePoint.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		int dimension = 0;
		int pointNr;
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		if (unchangedBottomPoint == 0)
		{
			borePoint.setBorePosition(point[0]);
			dimension = 1;
		}
		double lastBorePosition = borePoint.getBorePosition();

		borePoint = (BorePoint) sortedPoints[referencePointNr() - 1];
		double priorBorePosition = borePoint.getBorePosition();
		for (; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			borePoint = (BorePoint) sortedPoints[pointNr - 1];
			borePoint.setBorePosition(priorBorePosition
					+ point[dimension] * (lastBorePosition - priorBorePosition));
			priorBorePosition = borePoint.getBorePosition();
		}
		calculator.getInstrument().updateComponents();
	}

	@Override
	public void setLowerBounds(double[] aLowerBounds)
	{
		if (unchangedBottomPoint == 0)
		{
			// Adjust first lower bound to keep bottom bore point below the bottom hole.
			// This strategy is inadequate for merged optimizers that may move
			// the bottom hole.
			PositionInterface[] sortedHoles = Instrument.sortList(calculator
					.getInstrument().getHole());
			double bottomHolePosition;
			if (sortedHoles.length > 1)
			{
				bottomHolePosition = sortedHoles[sortedHoles.length - 1].getBorePosition();
			}
			else {
				// No holes.  Use mid-point of bore.
				PositionInterface[] sortedPoints = Instrument.sortList(calculator
						.getInstrument().getBorePoint());
				bottomHolePosition = 0.5 * (sortedPoints[0].getBorePosition()
						+ sortedPoints[sortedPoints.length - 1].getBorePosition());
			}
			if (aLowerBounds[0] < bottomHolePosition + 0.012)
			{
				// Raise the lower bound to restrict bottom bore position.
				aLowerBounds[0] = bottomHolePosition + 0.012;
			}
		}
		super.setLowerBounds(aLowerBounds);
	}
}
