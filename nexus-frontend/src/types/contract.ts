export interface TestVariable {
  id: string;
  name: string;
  dataType: string;
  unit: string;
  minValue: number | null;
  maxValue: number | null;
}

export type ContractStatus = 'DRAFT' | 'PUBLISHED';

export interface DataContract {
  id: string;
  tenantId: string;
  name: string;
  description: string;
  status: ContractStatus;
  businessGoals: unknown;
  sharingRules: unknown;
  testVariables: TestVariable[];
  version: number;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CreateContractRequest {
  name: string;
  description: string;
  businessGoals?: unknown;
  sharingRules?: unknown;
  testVariables?: Omit<TestVariable, 'id'>[];
}

export interface UpdateContractRequest {
  name?: string;
  description?: string;
  businessGoals?: unknown;
  sharingRules?: unknown;
  testVariables?: Omit<TestVariable, 'id'>[];
}
