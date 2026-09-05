/**
 * Builds the WebSocket root url for a location. An `https:` page must use `wss:`, otherwise the browser blocks
 * the connection as mixed content and the live terminal/dashboard never updates.
 */
export function toWebSocketRoot(loc: { protocol: string; host: string }): string {
  return (loc.protocol === 'https:' ? 'wss://' : 'ws://') + loc.host;
}
