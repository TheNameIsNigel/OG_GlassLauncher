package android.support.constraint.solver;

import android.support.constraint.solver.SolverVariable;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import java.util.Arrays;
import java.util.HashMap;

public class LinearSystem {
    private static final boolean DEBUG = false;
    private static int POOL_SIZE = 1000;
    private int TABLE_SIZE;
    private boolean[] mAlreadyTestedCandidates;
    final Cache mCache;
    private Goal mGoal;
    private int mMaxColumns;
    private int mMaxRows;
    int mNumColumns;
    private int mNumRows;
    private SolverVariable[] mPoolVariables;
    private int mPoolVariablesCount;
    private ArrayRow[] mRows;
    private HashMap<String, SolverVariable> mVariables;
    int mVariablesID;
    private ArrayRow[] tempClientsCopy;

    public LinearSystem() {
        this.mVariablesID = 0;
        this.mVariables = null;
        this.mGoal = new Goal();
        this.TABLE_SIZE = 32;
        this.mMaxColumns = this.TABLE_SIZE;
        this.mRows = null;
        this.mAlreadyTestedCandidates = new boolean[this.TABLE_SIZE];
        this.mNumColumns = 1;
        this.mNumRows = 0;
        this.mMaxRows = this.TABLE_SIZE;
        this.mPoolVariables = new SolverVariable[POOL_SIZE];
        this.mPoolVariablesCount = 0;
        this.tempClientsCopy = new ArrayRow[this.TABLE_SIZE];
        this.mRows = new ArrayRow[this.TABLE_SIZE];
        releaseRows();
        this.mCache = new Cache();
    }

    private void increaseTableSize() {
        this.TABLE_SIZE *= 2;
        this.mRows = (ArrayRow[]) Arrays.copyOf(this.mRows, this.TABLE_SIZE);
        this.mCache.mIndexedVariables = (SolverVariable[]) Arrays.copyOf(this.mCache.mIndexedVariables, this.TABLE_SIZE);
        this.mAlreadyTestedCandidates = new boolean[this.TABLE_SIZE];
        this.mMaxColumns = this.TABLE_SIZE;
        this.mMaxRows = this.TABLE_SIZE;
        this.mGoal.variables.clear();
    }

    private void releaseRows() {
        for (int i = 0; i < this.mRows.length; i++) {
            ArrayRow row = this.mRows[i];
            if (row != null) {
                this.mCache.arrayRowPool.release(row);
            }
            this.mRows[i] = null;
        }
    }

    public void reset() {
        for (SolverVariable variable : this.mCache.mIndexedVariables) {
            if (variable != null) {
                variable.reset();
            }
        }
        this.mCache.solverVariablePool.releaseAll(this.mPoolVariables, this.mPoolVariablesCount);
        this.mPoolVariablesCount = 0;
        Arrays.fill(this.mCache.mIndexedVariables, (Object) null);
        if (this.mVariables != null) {
            this.mVariables.clear();
        }
        this.mVariablesID = 0;
        this.mGoal.variables.clear();
        this.mNumColumns = 1;
        for (int i = 0; i < this.mNumRows; i++) {
            this.mRows[i].used = false;
        }
        releaseRows();
        this.mNumRows = 0;
    }

    public SolverVariable createObjectVariable(Object anchor) {
        if (anchor == null) {
            return null;
        }
        if (this.mNumColumns + 1 >= this.mMaxColumns) {
            increaseTableSize();
        }
        SolverVariable variable = null;
        if (anchor instanceof ConstraintAnchor) {
            variable = ((ConstraintAnchor) anchor).getSolverVariable();
            if (variable == null) {
                ((ConstraintAnchor) anchor).resetSolverVariable(this.mCache);
                variable = ((ConstraintAnchor) anchor).getSolverVariable();
            }
            if (variable.id == -1 || variable.id > this.mVariablesID || this.mCache.mIndexedVariables[variable.id] == null) {
                if (variable.id != -1) {
                    variable.reset();
                }
                this.mVariablesID++;
                this.mNumColumns++;
                variable.id = this.mVariablesID;
                variable.mType = SolverVariable.Type.UNRESTRICTED;
                this.mCache.mIndexedVariables[this.mVariablesID] = variable;
            }
        }
        return variable;
    }

    public ArrayRow createRow() {
        ArrayRow row = this.mCache.arrayRowPool.acquire();
        if (row == null) {
            return new ArrayRow(this.mCache);
        }
        row.reset();
        return row;
    }

    public SolverVariable createSlackVariable() {
        if (this.mNumColumns + 1 >= this.mMaxColumns) {
            increaseTableSize();
        }
        SolverVariable variable = acquireSolverVariable(SolverVariable.Type.SLACK);
        this.mVariablesID++;
        this.mNumColumns++;
        variable.id = this.mVariablesID;
        this.mCache.mIndexedVariables[this.mVariablesID] = variable;
        return variable;
    }

    private void addError(ArrayRow row) {
        row.addError(createErrorVariable(), createErrorVariable());
    }

    private void addSingleError(ArrayRow row, int sign) {
        row.addSingleError(createErrorVariable(), sign);
    }

    private SolverVariable createVariable(String name, SolverVariable.Type type) {
        if (this.mNumColumns + 1 >= this.mMaxColumns) {
            increaseTableSize();
        }
        SolverVariable variable = acquireSolverVariable(type);
        variable.setName(name);
        this.mVariablesID++;
        this.mNumColumns++;
        variable.id = this.mVariablesID;
        if (this.mVariables == null) {
            this.mVariables = new HashMap<>();
        }
        this.mVariables.put(name, variable);
        this.mCache.mIndexedVariables[this.mVariablesID] = variable;
        return variable;
    }

    public SolverVariable createErrorVariable() {
        if (this.mNumColumns + 1 >= this.mMaxColumns) {
            increaseTableSize();
        }
        SolverVariable variable = acquireSolverVariable(SolverVariable.Type.ERROR);
        this.mVariablesID++;
        this.mNumColumns++;
        variable.id = this.mVariablesID;
        this.mCache.mIndexedVariables[this.mVariablesID] = variable;
        return variable;
    }

    private SolverVariable acquireSolverVariable(SolverVariable.Type type) {
        SolverVariable variable = this.mCache.solverVariablePool.acquire();
        if (variable != null) {
            variable.reset();
            variable.setType(type);
        } else {
            variable = new SolverVariable(type);
        }
        if (this.mPoolVariablesCount >= POOL_SIZE) {
            POOL_SIZE *= 2;
            this.mPoolVariables = (SolverVariable[]) Arrays.copyOf(this.mPoolVariables, POOL_SIZE);
        }
        SolverVariable[] solverVariableArr = this.mPoolVariables;
        int i = this.mPoolVariablesCount;
        this.mPoolVariablesCount = i + 1;
        solverVariableArr[i] = variable;
        return variable;
    }

    /* access modifiers changed from: package-private */
    public Goal getGoal() {
        return this.mGoal;
    }

    /* access modifiers changed from: package-private */
    public ArrayRow getRow(int n) {
        return this.mRows[n];
    }

    /* access modifiers changed from: package-private */
    public float getValueFor(String name) {
        SolverVariable v = getVariable(name, SolverVariable.Type.UNRESTRICTED);
        if (v != null) {
            return v.computedValue;
        }
        return 0.0f;
    }

    public int getObjectVariableValue(Object anchor) {
        SolverVariable variable = ((ConstraintAnchor) anchor).getSolverVariable();
        if (variable == null) {
            return 0;
        }
        return (int) (variable.computedValue + 0.5f);
    }

    /* access modifiers changed from: package-private */
    public SolverVariable getVariable(String name, SolverVariable.Type type) {
        if (this.mVariables == null) {
            this.mVariables = new HashMap<>();
        }
        SolverVariable variable = this.mVariables.get(name);
        if (variable != null) {
            return variable;
        }
        return createVariable(name, type);
    }

    /* access modifiers changed from: package-private */
    public void rebuildGoalFromErrors() {
        this.mGoal.updateFromSystem(this);
    }

    public void minimize() throws Exception {
        minimizeGoal(this.mGoal);
    }

    /* access modifiers changed from: package-private */
    public void minimizeGoal(Goal goal) throws Exception {
        goal.updateFromSystem(this);
        enforceBFS(goal);
        optimize(goal);
        computeValues();
    }

    private void updateRowFromVariables(ArrayRow row) {
        if (this.mNumRows > 0) {
            row.variables.updateFromSystem(row, this.mRows);
            if (row.variables.currentSize == 0) {
                row.isSimpleDefinition = true;
            }
        }
    }

    public void addConstraint(ArrayRow row) {
        if (row != null) {
            if (this.mNumRows + 1 >= this.mMaxRows || this.mNumColumns + 1 >= this.mMaxColumns) {
                increaseTableSize();
            }
            if (!row.isSimpleDefinition) {
                updateRowFromVariables(row);
                row.ensurePositiveConstant();
                row.pickRowVariable();
                if (!row.hasKeyVariable()) {
                    return;
                }
            }
            if (this.mRows[this.mNumRows] != null) {
                this.mCache.arrayRowPool.release(this.mRows[this.mNumRows]);
            }
            if (!row.isSimpleDefinition) {
                row.updateClientEquations();
            }
            this.mRows[this.mNumRows] = row;
            row.variable.definitionId = this.mNumRows;
            this.mNumRows++;
            int count = row.variable.mClientEquationsCount;
            if (count > 0) {
                while (this.tempClientsCopy.length < count) {
                    this.tempClientsCopy = new ArrayRow[(this.tempClientsCopy.length * 2)];
                }
                ArrayRow[] clients = this.tempClientsCopy;
                for (int i = 0; i < count; i++) {
                    clients[i] = row.variable.mClientEquations[i];
                }
                for (int i2 = 0; i2 < count; i2++) {
                    ArrayRow client = clients[i2];
                    if (client != row) {
                        client.variables.updateFromRow(client, row);
                        client.updateClientEquations();
                    }
                }
            }
        }
    }

    private int optimize(Goal goal) {
        boolean done = false;
        int tries = 0;
        for (int i = 0; i < this.mNumColumns; i++) {
            this.mAlreadyTestedCandidates[i] = false;
        }
        int tested = 0;
        while (!done) {
            tries++;
            SolverVariable pivotCandidate = goal.getPivotCandidate();
            if (pivotCandidate != null) {
                if (!this.mAlreadyTestedCandidates[pivotCandidate.id]) {
                    this.mAlreadyTestedCandidates[pivotCandidate.id] = true;
                    tested++;
                    if (tested >= this.mNumColumns) {
                        done = true;
                    }
                } else {
                    pivotCandidate = null;
                }
            }
            if (pivotCandidate == null) {
                done = true;
            } else {
                float min = Float.MAX_VALUE;
                int pivotRowIndex = -1;
                for (int i2 = 0; i2 < this.mNumRows; i2++) {
                    ArrayRow current = this.mRows[i2];
                    if (current.variable.mType != SolverVariable.Type.UNRESTRICTED && current.hasVariable(pivotCandidate)) {
                        float a_j = current.variables.get(pivotCandidate);
                        if (a_j < 0.0f) {
                            float value = (-current.constantValue) / a_j;
                            if (value < min) {
                                min = value;
                                pivotRowIndex = i2;
                            }
                        }
                    }
                }
                if (pivotRowIndex <= -1) {
                    done = true;
                } else {
                    ArrayRow pivotEquation = this.mRows[pivotRowIndex];
                    pivotEquation.variable.definitionId = -1;
                    pivotEquation.pivot(pivotCandidate);
                    pivotEquation.variable.definitionId = pivotRowIndex;
                    for (int i3 = 0; i3 < this.mNumRows; i3++) {
                        this.mRows[i3].updateRowWithEquation(pivotEquation);
                    }
                    goal.updateFromSystem(this);
                    try {
                        enforceBFS(goal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tries;
    }

    private int enforceBFS(Goal goal) throws Exception {
        int tries = 0;
        boolean infeasibleSystem = false;
        int i = 0;
        while (true) {
            if (i < this.mNumRows) {
                if (this.mRows[i].variable.mType != SolverVariable.Type.UNRESTRICTED && this.mRows[i].constantValue < 0.0f) {
                    infeasibleSystem = true;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (infeasibleSystem) {
            boolean done = false;
            tries = 0;
            while (!done) {
                tries++;
                float min = Float.MAX_VALUE;
                int strength = 0;
                int pivotRowIndex = -1;
                int pivotColumnIndex = -1;
                for (int i2 = 0; i2 < this.mNumRows; i2++) {
                    ArrayRow current = this.mRows[i2];
                    if (current.variable.mType != SolverVariable.Type.UNRESTRICTED && current.constantValue < 0.0f) {
                        for (int j = 1; j < this.mNumColumns; j++) {
                            SolverVariable candidate = this.mCache.mIndexedVariables[j];
                            float a_j = current.variables.get(candidate);
                            if (a_j > 0.0f) {
                                for (int k = 0; k < 6; k++) {
                                    float value = candidate.strengthVector[k] / a_j;
                                    if ((value < min && k == strength) || k > strength) {
                                        min = value;
                                        pivotRowIndex = i2;
                                        pivotColumnIndex = j;
                                        strength = k;
                                    }
                                }
                            }
                        }
                    }
                }
                if (pivotRowIndex == -1) {
                    done = true;
                } else {
                    ArrayRow pivotEquation = this.mRows[pivotRowIndex];
                    pivotEquation.variable.definitionId = -1;
                    pivotEquation.pivot(this.mCache.mIndexedVariables[pivotColumnIndex]);
                    pivotEquation.variable.definitionId = pivotRowIndex;
                    for (int i3 = 0; i3 < this.mNumRows; i3++) {
                        this.mRows[i3].updateRowWithEquation(pivotEquation);
                    }
                    goal.updateFromSystem(this);
                }
            }
        }
        int i4 = 0;
        while (true) {
            if (i4 < this.mNumRows) {
                if (this.mRows[i4].variable.mType != SolverVariable.Type.UNRESTRICTED && this.mRows[i4].constantValue < 0.0f) {
                    break;
                }
                i4++;
            } else {
                break;
            }
        }
        return tries;
    }

    private void computeValues() {
        for (int i = 0; i < this.mNumRows; i++) {
            ArrayRow row = this.mRows[i];
            row.variable.computedValue = row.constantValue;
        }
    }

    private void displayRows() {
        displaySolverVariables();
        String s = "";
        for (int i = 0; i < this.mNumRows; i++) {
            s = (s + this.mRows[i]) + "\n";
        }
        if (this.mGoal.variables.size() != 0) {
            s = s + this.mGoal + "\n";
        }
        System.out.println(s);
    }

    /* access modifiers changed from: package-private */
    public void displayReadableRows() {
        displaySolverVariables();
        String s = "";
        for (int i = 0; i < this.mNumRows; i++) {
            s = (s + this.mRows[i].toReadableString()) + "\n";
        }
        if (this.mGoal != null) {
            s = s + this.mGoal + "\n";
        }
        System.out.println(s);
    }

    public void displayVariablesReadableRows() {
        displaySolverVariables();
        String s = "";
        for (int i = 0; i < this.mNumRows; i++) {
            if (this.mRows[i].variable.mType == SolverVariable.Type.UNRESTRICTED) {
                s = (s + this.mRows[i].toReadableString()) + "\n";
            }
        }
        if (this.mGoal.variables.size() != 0) {
            s = s + this.mGoal + "\n";
        }
        System.out.println(s);
    }

    public int getMemoryUsed() {
        int actualRowSize = 0;
        for (int i = 0; i < this.mNumRows; i++) {
            if (this.mRows[i] != null) {
                actualRowSize += this.mRows[i].sizeInBytes();
            }
        }
        return actualRowSize;
    }

    public int getNumEquations() {
        return this.mNumRows;
    }

    public int getNumVariables() {
        return this.mVariablesID;
    }

    /* access modifiers changed from: package-private */
    public void displaySystemInformations() {
        int rowSize = 0;
        for (int i = 0; i < this.TABLE_SIZE; i++) {
            if (this.mRows[i] != null) {
                rowSize += this.mRows[i].sizeInBytes();
            }
        }
        int actualRowSize = 0;
        for (int i2 = 0; i2 < this.mNumRows; i2++) {
            if (this.mRows[i2] != null) {
                actualRowSize += this.mRows[i2].sizeInBytes();
            }
        }
        System.out.println("Linear System -> Table size: " + this.TABLE_SIZE + " (" + getDisplaySize(this.TABLE_SIZE * this.TABLE_SIZE) + ") -- row sizes: " + getDisplaySize(rowSize) + ", actual size: " + getDisplaySize(actualRowSize) + " rows: " + this.mNumRows + "/" + this.mMaxRows + " cols: " + this.mNumColumns + "/" + this.mMaxColumns + " " + 0 + " occupied cells, " + getDisplaySize(0));
    }

    private void displaySolverVariables() {
        String s = "Display Rows (" + this.mNumRows + "x" + this.mNumColumns + ") :\n\t | C | ";
        for (int i = 1; i <= this.mNumColumns; i++) {
            s = (s + this.mCache.mIndexedVariables[i]) + " | ";
        }
        System.out.println(s + "\n");
    }

    private String getDisplaySize(int n) {
        int mb = ((n * 4) / 1024) / 1024;
        if (mb > 0) {
            return "" + mb + " Mb";
        }
        int kb = (n * 4) / 1024;
        if (kb <= 0) {
            return "" + (n * 4) + " bytes";
        }
        return "" + kb + " Kb";
    }

    public Cache getCache() {
        return this.mCache;
    }

    public void addGreaterThan(SolverVariable a, SolverVariable b, int margin, int strength) {
        ArrayRow row = createRow();
        SolverVariable slack = createSlackVariable();
        slack.strength = strength;
        row.createRowGreaterThan(a, b, slack, margin);
        addConstraint(row);
    }

    public void addLowerThan(SolverVariable a, SolverVariable b, int margin, int strength) {
        ArrayRow row = createRow();
        SolverVariable slack = createSlackVariable();
        slack.strength = strength;
        row.createRowLowerThan(a, b, slack, margin);
        addConstraint(row);
    }

    public void addCentering(SolverVariable a, SolverVariable b, int m1, float bias, SolverVariable c, SolverVariable d, int m2, int strength) {
        ArrayRow row = createRow();
        row.createRowCentering(a, b, m1, bias, c, d, m2);
        SolverVariable error1 = createErrorVariable();
        SolverVariable error2 = createErrorVariable();
        error1.strength = strength;
        error2.strength = strength;
        row.addError(error1, error2);
        addConstraint(row);
    }

    public ArrayRow addEquality(SolverVariable a, SolverVariable b, int margin, int strength) {
        ArrayRow row = createRow();
        row.createRowEquals(a, b, margin);
        SolverVariable error1 = createErrorVariable();
        SolverVariable error2 = createErrorVariable();
        error1.strength = strength;
        error2.strength = strength;
        row.addError(error1, error2);
        addConstraint(row);
        return row;
    }

    public void addEquality(SolverVariable a, int value) {
        int idx = a.definitionId;
        if (a.definitionId == -1) {
            ArrayRow row = createRow();
            row.createRowDefinition(a, value);
            addConstraint(row);
            return;
        }
        ArrayRow row2 = this.mRows[idx];
        if (!row2.isSimpleDefinition) {
            ArrayRow newRow = createRow();
            newRow.createRowEquals(a, value);
            addConstraint(newRow);
            return;
        }
        row2.constantValue = (float) value;
    }

    public static ArrayRow createRowEquals(LinearSystem linearSystem, SolverVariable variableA, SolverVariable variableB, int margin, boolean withError) {
        ArrayRow row = linearSystem.createRow();
        row.createRowEquals(variableA, variableB, margin);
        if (withError) {
            linearSystem.addSingleError(row, 1);
        }
        return row;
    }

    public static ArrayRow createRowDimensionPercent(LinearSystem linearSystem, SolverVariable variableA, SolverVariable variableB, SolverVariable variableC, float percent, boolean withError) {
        ArrayRow row = linearSystem.createRow();
        if (withError) {
            linearSystem.addError(row);
        }
        return row.createRowDimensionPercent(variableA, variableB, variableC, percent);
    }

    public static ArrayRow createRowGreaterThan(LinearSystem linearSystem, SolverVariable variableA, SolverVariable variableB, int margin, boolean withError) {
        SolverVariable slack = linearSystem.createSlackVariable();
        ArrayRow row = linearSystem.createRow();
        row.createRowGreaterThan(variableA, variableB, slack, margin);
        if (withError) {
            linearSystem.addSingleError(row, (int) (-1.0f * row.variables.get(slack)));
        }
        return row;
    }

    public static ArrayRow createRowLowerThan(LinearSystem linearSystem, SolverVariable variableA, SolverVariable variableB, int margin, boolean withError) {
        SolverVariable slack = linearSystem.createSlackVariable();
        ArrayRow row = linearSystem.createRow();
        row.createRowLowerThan(variableA, variableB, slack, margin);
        if (withError) {
            linearSystem.addSingleError(row, (int) (-1.0f * row.variables.get(slack)));
        }
        return row;
    }

    public static ArrayRow createRowCentering(LinearSystem linearSystem, SolverVariable variableA, SolverVariable variableB, int marginA, float bias, SolverVariable variableC, SolverVariable variableD, int marginB, boolean withError) {
        ArrayRow row = linearSystem.createRow();
        row.createRowCentering(variableA, variableB, marginA, bias, variableC, variableD, marginB);
        if (withError) {
            SolverVariable error1 = linearSystem.createErrorVariable();
            SolverVariable error2 = linearSystem.createErrorVariable();
            error1.strength = 4;
            error2.strength = 4;
            row.addError(error1, error2);
        }
        return row;
    }
}
