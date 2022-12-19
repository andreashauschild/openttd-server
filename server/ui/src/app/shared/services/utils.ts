export const clone = <T>(item: T): T => {
  if (item) {
    return JSON.parse(JSON.stringify(item));
  }
  return item;
}
