import { SettingsRepository } from '../data/settings-repository'

export class IdentityManager {
  constructor(private settingsRepo: SettingsRepository) {}

  getOrCreateLocalUserId(): string {
    const existing = this.settingsRepo.getLocalUser()
    if (existing) return existing.id

    const id = crypto.randomUUID()
    const createdAt = new Date().toISOString()
    this.settingsRepo.insertLocalUser(id, createdAt)
    return id
  }
}
