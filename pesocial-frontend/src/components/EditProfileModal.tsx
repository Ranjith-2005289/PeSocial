import { useState, useEffect } from 'react';

interface Props {
  open: boolean;
  onClose: () => void;
  initial: { username: string; handle: string; bio?: string };
  onSave: (fields: { username: string; handle: string; bio?: string }) => void;
  loading?: boolean;
  error?: string;
}

export default function EditProfileModal({ open, onClose, initial, onSave, loading, error }: Props) {
  const [username, setUsername] = useState<string>(initial.username ?? '');
  const [handle, setHandle] = useState<string>(initial.handle ?? '');
  const [bio, setBio] = useState<string>(initial.bio ?? '');

  useEffect(() => {
    if (open) {
      // Reset form only when modal opens
      setUsername(initial.username ?? '');
      setHandle(initial.handle ?? '');
      setBio(initial.bio ?? '');
    }
  }, [open]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4" onClick={onClose}>
      <div className="w-full max-w-md rounded-2xl border border-white/20 bg-zinc-950 p-6 text-white shadow-2xl" onClick={(e) => e.stopPropagation()}>
        <h2 className="mb-4 text-xl font-semibold">Edit Profile</h2>
        <form
          onSubmit={e => {
            e.preventDefault();
            onSave({ username, handle, bio });
          }}
        >
          <div className="mb-3">
            <label className="block text-sm font-medium text-white/80">Display Name</label>
            <input
              className="mt-1 w-full rounded-lg border border-white/20 bg-white/5 px-3 py-2 outline-none ring-0 transition focus:border-white/40"
              value={username}
              onChange={e => setUsername(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          <div className="mb-3">
            <label className="block text-sm font-medium text-white/80">Handle</label>
            <input
              className="mt-1 w-full rounded-lg border border-white/20 bg-white/5 px-3 py-2 outline-none ring-0 transition focus:border-white/40"
              value={handle}
              onChange={e => setHandle(e.target.value)}
              required
              pattern="^@[a-zA-Z0-9_]{3,20}$"
              title="Must start with @ and contain 3-20 letters, numbers, or underscores"
              disabled={loading}
            />
          </div>
          <div className="mb-3">
            <label className="block text-sm font-medium text-white/80">Bio</label>
            <textarea
              className="mt-1 w-full rounded-lg border border-white/20 bg-white/5 px-3 py-2 outline-none ring-0 transition focus:border-white/40"
              value={bio}
              onChange={e => setBio(e.target.value)}
              rows={3}
              maxLength={160}
              disabled={loading}
            />
            <div className="mt-1 text-right text-xs text-white/60">{bio.length}/160</div>
          </div>
          {error && <div className="text-red-500 text-sm mb-2">{error}</div>}
          <div className="flex justify-end gap-2 mt-4">
            <button type="button" className="rounded-lg border border-white/30 px-4 py-2 text-sm hover:bg-white/10" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="rounded-lg bg-white px-4 py-2 text-sm font-medium text-black disabled:opacity-60" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
