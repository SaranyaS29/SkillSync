const inr = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 })

export function formatMoney(value) {
  const amount = Number(value)
  return inr.format(Number.isFinite(amount) ? amount : 0)
}
