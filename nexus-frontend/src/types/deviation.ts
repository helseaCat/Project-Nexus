import type { Severity } from './expectation';

export interface Deviation {
  id: string;
  payloadId: string;
  expectationId: string | null;
  dataContractId: string;
  severity: Severity;
  description: string;
  detectedValue: string;
  expectedValue: string;
  createdAt: string;
}
