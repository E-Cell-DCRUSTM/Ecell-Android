/** Complaint titles and descriptions are private and must never be copied to push payloads. */
export function complaintUpdateNotification(): { title: string; body: string } {
  return {
    title: "Complaint update",
    body: "Your complaint status or response changed. Sign in to view details.",
  };
}

export function membershipApprovalNotification(): { title: string; body: string } {
  return {
    title: "E-Cell membership approved",
    body: "Your membership is active. Sign in to view your E-Cell Member ID.",
  };
}
