import {LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, SESSION_STORAGE_KEY} from '../model/constants';

/**
 * Reads the session id from the localStorage. Sessions stored under the legacy key (the old header name) are
 * migrated to the new key once, so an upgrade does not log the user out.
 */
export function readSessionId(): string | null {
  const sessionId = localStorage.getItem(SESSION_STORAGE_KEY);
  if (sessionId) {
    return sessionId;
  }
  const legacySessionId = localStorage.getItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID);
  if (legacySessionId) {
    localStorage.setItem(SESSION_STORAGE_KEY, legacySessionId);
    localStorage.removeItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID);
    return legacySessionId;
  }
  return null;
}

export function writeSessionId(sessionId: string): void {
  localStorage.setItem(SESSION_STORAGE_KEY, sessionId);
}

export function clearSessionId(): void {
  localStorage.removeItem(SESSION_STORAGE_KEY);
  localStorage.removeItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID);
}
