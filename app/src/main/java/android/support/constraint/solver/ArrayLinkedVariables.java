package android.support.constraint.solver;

import android.support.constraint.solver.SolverVariable;
import java.util.Arrays;

public class ArrayLinkedVariables {
    private static final boolean DEBUG = false;
    private static final int NONE = -1;
    private int ROW_SIZE = 8;
    private SolverVariable candidate = null;
    int currentSize = 0;
    private int[] mArrayIndices = new int[this.ROW_SIZE];
    private int[] mArrayNextIndices = new int[this.ROW_SIZE];
    private float[] mArrayValues = new float[this.ROW_SIZE];
    private final Cache mCache;
    private boolean mDidFillOnce = false;
    private int mHead = -1;
    private int mLast = -1;
    private final ArrayRow mRow;

    ArrayLinkedVariables(ArrayRow arrayRow, Cache cache) {
        this.mRow = arrayRow;
        this.mCache = cache;
    }

    public final void put(SolverVariable variable, float value) {
        if (value == 0.0f) {
            remove(variable);
        } else if (this.mHead != -1) {
            int current = this.mHead;
            int previous = -1;
            int counter = 0;
            while (current != -1 && counter < this.currentSize) {
                if (this.mArrayIndices[current] != variable.id) {
                    if (this.mArrayIndices[current] < variable.id) {
                        previous = current;
                    }
                    current = this.mArrayNextIndices[current];
                    counter++;
                } else {
                    this.mArrayValues[current] = value;
                    return;
                }
            }
            int availableIndice = this.mLast + 1;
            if (this.mDidFillOnce) {
                if (this.mArrayIndices[this.mLast] != -1) {
                    availableIndice = this.mArrayIndices.length;
                } else {
                    availableIndice = this.mLast;
                }
            }
            if (availableIndice >= this.mArrayIndices.length && this.currentSize < this.mArrayIndices.length) {
                int i = 0;
                while (true) {
                    if (i < this.mArrayIndices.length) {
                        if (this.mArrayIndices[i] == -1) {
                            availableIndice = i;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
            if (availableIndice >= this.mArrayIndices.length) {
                availableIndice = this.mArrayIndices.length;
                this.ROW_SIZE *= 2;
                this.mDidFillOnce = false;
                this.mLast = availableIndice - 1;
                this.mArrayValues = Arrays.copyOf(this.mArrayValues, this.ROW_SIZE);
                this.mArrayIndices = Arrays.copyOf(this.mArrayIndices, this.ROW_SIZE);
                this.mArrayNextIndices = Arrays.copyOf(this.mArrayNextIndices, this.ROW_SIZE);
            }
            this.mArrayIndices[availableIndice] = variable.id;
            this.mArrayValues[availableIndice] = value;
            if (previous == -1) {
                this.mArrayNextIndices[availableIndice] = this.mHead;
                this.mHead = availableIndice;
            } else {
                this.mArrayNextIndices[availableIndice] = this.mArrayNextIndices[previous];
                this.mArrayNextIndices[previous] = availableIndice;
            }
            this.currentSize++;
            if (!this.mDidFillOnce) {
                this.mLast++;
            }
            if (this.currentSize >= this.mArrayIndices.length) {
                this.mDidFillOnce = true;
            }
        } else {
            this.mHead = 0;
            this.mArrayValues[this.mHead] = value;
            this.mArrayIndices[this.mHead] = variable.id;
            this.mArrayNextIndices[this.mHead] = -1;
            this.currentSize++;
            if (!this.mDidFillOnce) {
                this.mLast++;
            }
        }
    }

    public final void add(SolverVariable variable, float value) {
        if (value != 0.0f) {
            if (this.mHead != -1) {
                int current = this.mHead;
                int previous = -1;
                int counter = 0;
                while (current != -1 && counter < this.currentSize) {
                    int idx = this.mArrayIndices[current];
                    if (idx != variable.id) {
                        if (this.mArrayIndices[current] < variable.id) {
                            previous = current;
                        }
                        current = this.mArrayNextIndices[current];
                        counter++;
                    } else {
                        float[] fArr = this.mArrayValues;
                        fArr[current] = fArr[current] + value;
                        if (this.mArrayValues[current] == 0.0f) {
                            if (current != this.mHead) {
                                this.mArrayNextIndices[previous] = this.mArrayNextIndices[current];
                            } else {
                                this.mHead = this.mArrayNextIndices[current];
                            }
                            this.mCache.mIndexedVariables[idx].removeClientEquation(this.mRow);
                            if (this.mDidFillOnce) {
                                this.mLast = current;
                            }
                            this.currentSize--;
                            return;
                        }
                        return;
                    }
                }
                int availableIndice = this.mLast + 1;
                if (this.mDidFillOnce) {
                    if (this.mArrayIndices[this.mLast] != -1) {
                        availableIndice = this.mArrayIndices.length;
                    } else {
                        availableIndice = this.mLast;
                    }
                }
                if (availableIndice >= this.mArrayIndices.length && this.currentSize < this.mArrayIndices.length) {
                    int i = 0;
                    while (true) {
                        if (i < this.mArrayIndices.length) {
                            if (this.mArrayIndices[i] == -1) {
                                availableIndice = i;
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                }
                if (availableIndice >= this.mArrayIndices.length) {
                    availableIndice = this.mArrayIndices.length;
                    this.ROW_SIZE *= 2;
                    this.mDidFillOnce = false;
                    this.mLast = availableIndice - 1;
                    this.mArrayValues = Arrays.copyOf(this.mArrayValues, this.ROW_SIZE);
                    this.mArrayIndices = Arrays.copyOf(this.mArrayIndices, this.ROW_SIZE);
                    this.mArrayNextIndices = Arrays.copyOf(this.mArrayNextIndices, this.ROW_SIZE);
                }
                this.mArrayIndices[availableIndice] = variable.id;
                this.mArrayValues[availableIndice] = value;
                if (previous == -1) {
                    this.mArrayNextIndices[availableIndice] = this.mHead;
                    this.mHead = availableIndice;
                } else {
                    this.mArrayNextIndices[availableIndice] = this.mArrayNextIndices[previous];
                    this.mArrayNextIndices[previous] = availableIndice;
                }
                this.currentSize++;
                if (!this.mDidFillOnce) {
                    this.mLast++;
                }
                if (this.mLast >= this.mArrayIndices.length) {
                    this.mDidFillOnce = true;
                    this.mLast = this.mArrayIndices.length - 1;
                    return;
                }
                return;
            }
            this.mHead = 0;
            this.mArrayValues[this.mHead] = value;
            this.mArrayIndices[this.mHead] = variable.id;
            this.mArrayNextIndices[this.mHead] = -1;
            this.currentSize++;
            if (!this.mDidFillOnce) {
                this.mLast++;
            }
        }
    }

    public final float remove(SolverVariable variable) {
        if (this.candidate == variable) {
            this.candidate = null;
        }
        if (this.mHead == -1) {
            return 0.0f;
        }
        int current = this.mHead;
        int previous = -1;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            int idx = this.mArrayIndices[current];
            if (idx != variable.id) {
                previous = current;
                current = this.mArrayNextIndices[current];
                counter++;
            } else {
                if (current != this.mHead) {
                    this.mArrayNextIndices[previous] = this.mArrayNextIndices[current];
                } else {
                    this.mHead = this.mArrayNextIndices[current];
                }
                this.mCache.mIndexedVariables[idx].removeClientEquation(this.mRow);
                this.currentSize--;
                this.mArrayIndices[current] = -1;
                if (this.mDidFillOnce) {
                    this.mLast = current;
                }
                return this.mArrayValues[current];
            }
        }
        return 0.0f;
    }

    public final void clear() {
        this.mHead = -1;
        this.mLast = -1;
        this.mDidFillOnce = false;
        this.currentSize = 0;
    }

    /* access modifiers changed from: package-private */
    public final boolean containsKey(SolverVariable variable) {
        if (this.mHead == -1) {
            return false;
        }
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            if (this.mArrayIndices[current] == variable.id) {
                return true;
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasAtLeastOnePositiveVariable() {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            if (this.mArrayValues[current] > 0.0f) {
                return true;
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void invert() {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            float[] fArr = this.mArrayValues;
            fArr[current] = fArr[current] * -1.0f;
            current = this.mArrayNextIndices[current];
            counter++;
        }
    }

    /* access modifiers changed from: package-private */
    public void divideByAmount(float amount) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            float[] fArr = this.mArrayValues;
            fArr[current] = fArr[current] / amount;
            current = this.mArrayNextIndices[current];
            counter++;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateClientEquations(ArrayRow row) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            this.mCache.mIndexedVariables[this.mArrayIndices[current]].addClientEquation(row);
            current = this.mArrayNextIndices[current];
            counter++;
        }
    }

    /* access modifiers changed from: package-private */
    public SolverVariable pickPivotCandidate() {
        SolverVariable restrictedCandidate = null;
        SolverVariable unrestrictedCandidate = null;
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            float amount = this.mArrayValues[current];
            if (amount < 0.0f) {
                if (amount > (-0.001f)) {
                    this.mArrayValues[current] = 0.0f;
                    amount = 0.0f;
                }
            } else if (amount < 0.001f) {
                this.mArrayValues[current] = 0.0f;
                amount = 0.0f;
            }
            if (amount != 0.0f) {
                SolverVariable variable = this.mCache.mIndexedVariables[this.mArrayIndices[current]];
                if (variable.mType != SolverVariable.Type.UNRESTRICTED) {
                    if (amount < 0.0f && (restrictedCandidate == null || variable.strength < restrictedCandidate.strength)) {
                        restrictedCandidate = variable;
                    }
                } else if (amount < 0.0f) {
                    return variable;
                } else {
                    if (unrestrictedCandidate == null) {
                        unrestrictedCandidate = variable;
                    }
                }
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        if (unrestrictedCandidate == null) {
            return restrictedCandidate;
        }
        return unrestrictedCandidate;
    }

    /* access modifiers changed from: package-private */
    public void updateFromRow(ArrayRow self, ArrayRow definition) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            if (this.mArrayIndices[current] != definition.variable.id) {
                current = this.mArrayNextIndices[current];
                counter++;
            } else {
                float value = this.mArrayValues[current];
                remove(definition.variable);
                ArrayLinkedVariables definitionVariables = definition.variables;
                int definitionCurrent = definitionVariables.mHead;
                int definitionCounter = 0;
                while (definitionCurrent != -1 && definitionCounter < definitionVariables.currentSize) {
                    add(this.mCache.mIndexedVariables[definitionVariables.mArrayIndices[definitionCurrent]], definitionVariables.mArrayValues[definitionCurrent] * value);
                    definitionCurrent = definitionVariables.mArrayNextIndices[definitionCurrent];
                    definitionCounter++;
                }
                self.constantValue += definition.constantValue * value;
                definition.variable.removeClientEquation(self);
                current = this.mHead;
                counter = 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateFromSystem(ArrayRow self, ArrayRow[] rows) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            SolverVariable variable = this.mCache.mIndexedVariables[this.mArrayIndices[current]];
            if (variable.definitionId == -1) {
                current = this.mArrayNextIndices[current];
                counter++;
            } else {
                float value = this.mArrayValues[current];
                remove(variable);
                ArrayRow definition = rows[variable.definitionId];
                if (!definition.isSimpleDefinition) {
                    ArrayLinkedVariables definitionVariables = definition.variables;
                    int definitionCurrent = definitionVariables.mHead;
                    int definitionCounter = 0;
                    while (definitionCurrent != -1 && definitionCounter < definitionVariables.currentSize) {
                        add(this.mCache.mIndexedVariables[definitionVariables.mArrayIndices[definitionCurrent]], definitionVariables.mArrayValues[definitionCurrent] * value);
                        definitionCurrent = definitionVariables.mArrayNextIndices[definitionCurrent];
                        definitionCounter++;
                    }
                }
                self.constantValue += definition.constantValue * value;
                definition.variable.removeClientEquation(self);
                current = this.mHead;
                counter = 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SolverVariable getPivotCandidate() {
        if (this.candidate != null) {
            return this.candidate;
        }
        int current = this.mHead;
        int counter = 0;
        SolverVariable solverVariable = null;
        while (current != -1 && counter < this.currentSize) {
            if (this.mArrayValues[current] < 0.0f) {
                SolverVariable v = this.mCache.mIndexedVariables[this.mArrayIndices[current]];
                if (solverVariable == null || solverVariable.strength < v.strength) {
                    solverVariable = v;
                }
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return solverVariable;
    }

    /* access modifiers changed from: package-private */
    public final SolverVariable getVariable(int index) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            if (counter == index) {
                return this.mCache.mIndexedVariables[this.mArrayIndices[current]];
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final float getVariableValue(int index) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            if (counter == index) {
                return this.mArrayValues[current];
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return 0.0f;
    }

    public final float get(SolverVariable v) {
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            if (this.mArrayIndices[current] == v.id) {
                return this.mArrayValues[current];
            }
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    public int sizeInBytes() {
        return (this.mArrayIndices.length * 4 * 3) + 0 + 36;
    }

    public void display() {
        int count = this.currentSize;
        System.out.print("{ ");
        for (int i = 0; i < count; i++) {
            SolverVariable v = getVariable(i);
            if (v != null) {
                System.out.print(v + " = " + getVariableValue(i) + " ");
            }
        }
        System.out.println(" }");
    }

    public String toString() {
        String result = "";
        int current = this.mHead;
        int counter = 0;
        while (current != -1 && counter < this.currentSize) {
            result = ((result + " -> ") + this.mArrayValues[current] + " : ") + this.mCache.mIndexedVariables[this.mArrayIndices[current]];
            current = this.mArrayNextIndices[current];
            counter++;
        }
        return result;
    }
}
