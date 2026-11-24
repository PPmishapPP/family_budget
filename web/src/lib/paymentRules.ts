import { toIsoDate } from './format.ts'

export const PAYMENT_RULE_TYPES = ['MONTHLY', 'WEEKLY', 'DAILY', 'ONE', 'YEARLY'] as const

export type PaymentRuleType = (typeof PAYMENT_RULE_TYPES)[number]

export const PAYMENT_RULE_TYPE_LABELS: Record<PaymentRuleType, string> = {
  MONTHLY: 'Ежемесячно',
  WEEKLY: 'Еженедельно',
  DAILY: 'Ежедневно',
  ONE: 'Единоразово',
  YEARLY: 'Ежегодно',
}

export const PAYMENT_RULE_TYPE_OPTIONS = PAYMENT_RULE_TYPES.map((value) => ({
  value,
  label: PAYMENT_RULE_TYPE_LABELS[value],
}))

export function isPaymentRuleType(value: string): value is PaymentRuleType {
  return (PAYMENT_RULE_TYPES as readonly string[]).includes(value)
}

export function isRuleDue(nextDay: string): boolean {
  return nextDay <= toIsoDate(new Date())
}
