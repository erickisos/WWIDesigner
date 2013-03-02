package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wwidesigner.util.Constants.LengthType;

public class Constraints
{
	private Map<String, List<Constraint>> constraintsMap;
	private LengthType dimensionType;
	private int numberOfHoles;
	private String objectiveDisplayName;
	private String objectFunctionName;

	public Constraints(LengthType dimensionType)
	{
		constraintsMap = new LinkedHashMap<String, List<Constraint>>(6);
		this.dimensionType = dimensionType;
	}

	public Set<String> getCategories()
	{
		return constraintsMap.keySet();
	}

	public void addConstraint(Constraint newConstraint)
	{
		if (Constraint.isValid(newConstraint))
		{
			String category = newConstraint.getCategory();
			List<Constraint> catConstraints = getConstraints(category);
			catConstraints.add(newConstraint);
		}
	}

	public Constraint getConstraint(String category, int index)
	{
		return constraintsMap.get(category).get(index);
	}

	public void addConstraints(Constraints newConstraints)
	{
		Set<String> categories = newConstraints.getCategories();
		for (String category : categories)
		{
			List<Constraint> catConstraints = newConstraints
					.getConstraints(category);
			for (Constraint constraint : catConstraints)
			{
				addConstraint(constraint);
			}
		}
	}

	public List<Constraint> getConstraints(String category)
	{
		if (category == null || category.trim().length() == 0)
		{
			return null;
		}

		List<Constraint> catConstraints = constraintsMap.get(category);
		if (catConstraints == null)
		{
			catConstraints = new ArrayList<Constraint>();
			constraintsMap.put(category, catConstraints);
		}

		return catConstraints;
	}

	public void clearConstraints(String category)
	{
		constraintsMap.remove(category);
	}

	public int getNumberOfConstraints(String category)
	{
		return constraintsMap.get(category).size();
	}

	public int getNumberOfHoles()
	{
		return numberOfHoles;
	}

	public void setNumberOfHoles(int numberOfHoles)
	{
		this.numberOfHoles = numberOfHoles;
	}

	public String getObjectiveDisplayName()
	{
		return objectiveDisplayName;
	}

	public void setObjectiveDisplayName(String objectiveDisplayName)
	{
		this.objectiveDisplayName = objectiveDisplayName;
	}

}
