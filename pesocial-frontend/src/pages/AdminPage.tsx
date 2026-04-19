import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { adminService } from '../services/adminService'
import { parseApiError } from '../services/api'
import type { User } from '../types/user'

const statusBadgeClass = (status?: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'border-emerald-400/50 bg-emerald-500/15 text-emerald-100'
    case 'SUSPENDED':
      return 'border-amber-400/50 bg-amber-500/15 text-amber-100'
    case 'BANNED':
      return 'border-rose-400/50 bg-rose-500/15 text-rose-100'
    case 'DELETED':
      return 'border-slate-400/50 bg-slate-500/15 text-slate-100'
    default:
      return 'border-white/20 bg-white/10 text-white'
  }
}

export default function AdminPage() {
  const queryClient = useQueryClient()
  const [announcement, setAnnouncement] = useState('')

  const usersQuery = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminService.getAllUsers(),
  })

  const reportsQuery = useQuery({
    queryKey: ['admin-reports'],
    queryFn: () => adminService.reviewReports(),
  })

  const systemReportQuery = useQuery({
    queryKey: ['admin-system-report'],
    queryFn: () => adminService.getSystemReport(),
  })

  const approveCreatorMutation = useMutation({
    mutationFn: (userId: string) => adminService.approveCreator(userId),
    onSuccess: async () => {
      toast.success('Creator approved')
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
        queryClient.invalidateQueries({ queryKey: ['admin-reports'] }),
      ])
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const suspendMutation = useMutation({
    mutationFn: (userId: string) => adminService.suspendUser(userId),
    onSuccess: async () => {
      toast.success('User suspended')
      await queryClient.invalidateQueries({ queryKey: ['admin-users'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const banMutation = useMutation({
    mutationFn: (userId: string) => adminService.banUser(userId),
    onSuccess: async () => {
      toast.success('User banned')
      await queryClient.invalidateQueries({ queryKey: ['admin-users'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const announcementMutation = useMutation({
    mutationFn: (message: string) => adminService.sendAnnouncement(message),
    onSuccess: async () => {
      toast.success('Announcement sent')
      setAnnouncement('')
      await queryClient.invalidateQueries({ queryKey: ['admin-system-report'] })
    },
    onError: (error) => toast.error(parseApiError(error)),
  })

  const users = usersQuery.data ?? []
  const creatorCandidates = useMemo(
    () => users.filter((user) => user.role === 'CREATOR' && !user.verificationStatus),
    [users],
  )

  const renderUserCard = (user: User) => {
    const canApprove = user.role === 'CREATOR' && !user.verificationStatus

    return (
      <div key={user.id} className="rounded-xl border border-white/15 bg-white/5 p-4 shadow-lg">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h3 className="text-lg font-semibold text-white">{user.username || user.handle}</h3>
              <span className={`rounded-full border px-2 py-0.5 text-xs ${statusBadgeClass(user.accountStatus)}`}>
                {user.accountStatus || 'ACTIVE'}
              </span>
              <span className="rounded-full border border-cyan-400/40 bg-cyan-500/10 px-2 py-0.5 text-xs text-cyan-100">
                {user.role}
              </span>
              {user.verificationStatus && (
                <span className="rounded-full border border-emerald-400/40 bg-emerald-500/10 px-2 py-0.5 text-xs text-emerald-100">
                  Verified
                </span>
              )}
            </div>
            <p className="mt-1 text-sm text-white/70">{user.email}</p>
            <p className="text-xs text-white/50">{user.handle}</p>
          </div>

          <div className="flex flex-wrap gap-2">
            {canApprove && (
              <button
                onClick={() => approveCreatorMutation.mutate(user.id)}
                disabled={approveCreatorMutation.isPending}
                className="rounded-lg border border-emerald-300/50 bg-emerald-500/15 px-3 py-2 text-sm text-emerald-50 hover:bg-emerald-500/25 disabled:opacity-60"
              >
                Approve Creator
              </button>
            )}
            <button
              onClick={() => suspendMutation.mutate(user.id)}
              disabled={suspendMutation.isPending}
              className="rounded-lg border border-amber-300/50 bg-amber-500/15 px-3 py-2 text-sm text-amber-50 hover:bg-amber-500/25 disabled:opacity-60"
            >
              Suspend
            </button>
            <button
              onClick={() => banMutation.mutate(user.id)}
              disabled={banMutation.isPending}
              className="rounded-lg border border-rose-300/50 bg-rose-500/15 px-3 py-2 text-sm text-rose-50 hover:bg-rose-500/25 disabled:opacity-60"
            >
              Ban
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto min-h-screen w-full max-w-6xl p-4 text-white">
      <header className="mb-6 rounded-xl border border-white/15 bg-white/10 p-5 backdrop-blur">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.25em] text-white/60">Admin Console</p>
            <h1 className="text-3xl font-semibold">Moderation Dashboard</h1>
            <p className="mt-1 text-sm text-white/70">Approve creators, review reports, and manage account status.</p>
          </div>
          <div className="grid grid-cols-2 gap-3 text-sm text-white/80 sm:grid-cols-4">
            <div className="rounded-lg border border-white/10 bg-white/5 px-3 py-2">Users: {users.length}</div>
            <div className="rounded-lg border border-white/10 bg-white/5 px-3 py-2">Creator requests: {creatorCandidates.length}</div>
            <div className="rounded-lg border border-white/10 bg-white/5 px-3 py-2">Reports: {reportsQuery.data?.length ?? 0}</div>
            <div className="rounded-lg border border-white/10 bg-white/5 px-3 py-2">System: {systemReportQuery.data ?? 'Loading...'}</div>
          </div>
        </div>
      </header>

      <section className="mb-6 grid gap-4 md:grid-cols-2">
        <div className="rounded-xl border border-white/15 bg-white/5 p-4">
          <h2 className="text-lg font-semibold">System Report</h2>
          <p className="mt-2 whitespace-pre-wrap text-sm text-white/75">
            {systemReportQuery.isLoading ? 'Loading report...' : systemReportQuery.data ?? 'No report available.'}
          </p>
        </div>

        <div className="rounded-xl border border-white/15 bg-white/5 p-4">
          <h2 className="text-lg font-semibold">Announcement</h2>
          <textarea
            value={announcement}
            onChange={(event) => setAnnouncement(event.target.value)}
            placeholder="Write an admin announcement..."
            className="mt-2 h-28 w-full rounded-lg border border-white/15 bg-slate-950/40 p-3 text-sm text-white outline-none placeholder:text-white/40"
          />
          <div className="mt-3 flex justify-end">
            <button
              onClick={() => announcement.trim() && announcementMutation.mutate(announcement.trim())}
              disabled={announcementMutation.isPending || announcement.trim().length === 0}
              className="rounded-lg bg-cyan-500 px-4 py-2 text-sm font-medium text-white hover:bg-cyan-400 disabled:opacity-60"
            >
              {announcementMutation.isPending ? 'Sending...' : 'Send Announcement'}
            </button>
          </div>
        </div>
      </section>

      <section className="mb-6 rounded-xl border border-white/15 bg-white/5 p-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-lg font-semibold">Creator Approvals</h2>
          <button
            onClick={() => void queryClient.invalidateQueries({ queryKey: ['admin-users'] })}
            className="rounded-lg border border-white/20 px-3 py-2 text-sm hover:bg-white/10"
          >
            Refresh Users
          </button>
        </div>
        <div className="mt-4 space-y-3">
          {creatorCandidates.length === 0 ? (
            <p className="text-sm text-white/65">No pending creator approvals.</p>
          ) : (
            creatorCandidates.map(renderUserCard)
          )}
        </div>
      </section>

      <section className="grid gap-4 lg:grid-cols-2">
        <div className="rounded-xl border border-white/15 bg-white/5 p-4">
          <h2 className="text-lg font-semibold">All Users</h2>
          <div className="mt-4 space-y-3">
            {usersQuery.isLoading ? (
              <p className="text-sm text-white/65">Loading users...</p>
            ) : users.length === 0 ? (
              <p className="text-sm text-white/65">No users found.</p>
            ) : (
              users.map(renderUserCard)
            )}
          </div>
        </div>

        <div className="rounded-xl border border-white/15 bg-white/5 p-4">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-lg font-semibold">Review Reports</h2>
            <button
              onClick={() => void reportsQuery.refetch()}
              className="rounded-lg border border-white/20 px-3 py-2 text-sm hover:bg-white/10"
            >
              Refresh Reports
            </button>
          </div>
          <div className="mt-4 space-y-3">
            {reportsQuery.isLoading ? (
              <p className="text-sm text-white/65">Loading reports...</p>
            ) : (reportsQuery.data?.length ?? 0) === 0 ? (
              <p className="text-sm text-white/65">No moderation reports.</p>
            ) : (
              reportsQuery.data?.map((report, index) => (
                <div key={`${report}-${index}`} className="rounded-lg border border-white/10 bg-white/5 p-3 text-sm text-white/80">
                  {report}
                </div>
              ))
            )}
          </div>
        </div>
      </section>
    </div>
  )
}
