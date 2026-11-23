# API examples

All protected endpoints take `Authorization: Bearer <accessToken>`. Pagination uses Spring Data parameters such as `page`, `size`, and `sort`.

## Register and sign in

`POST /api/auth/register`

```json
{"fullName":"Sam Example","email":"sam@example.com","password":"use-a-long-password","role":"FREELANCER"}
```

`POST /api/auth/login`

```json
{"email":"sam@example.com","password":"use-a-long-password"}
```

Both return `accessToken`, `tokenType`, `expiresInSeconds`, and a user summary. `GET /api/auth/me` returns the authenticated account.

An authenticated account can change its password with `PUT /api/auth/change-password`:

```json
{"currentPassword":"the-current-password","newPassword":"a-new-password-with-10-chars"}
```

## Create a project

`POST /api/projects` (CLIENT)

```json
{"title":"Build a booking site","description":"Create a responsive booking experience.","projectType":"FIXED_PRICE","budgetMin":1500,"budgetMax":3000,"experienceLevel":"INTERMEDIATE","category":"Web development","deadline":"2027-02-28","requiredSkills":["React","Java"],"publish":true}
```

Browse with `GET /api/projects?q=booking&page=0&size=20`. Client projects are available at `GET /api/projects/mine`.

Clients can update draft or open projects with `PUT /api/projects/{id}` using the same request body. `PUT /api/projects/{id}/cancel` cancels an owned draft/open project only if it has no accepted proposal; pending proposals are declined and the freelancers are notified. Use `GET /api/projects/mine` to view the full client project list. Project budget values are shown in Indian rupees in the UI.

## Freelancer discovery and matching

Filter freelancer profiles with any combination of `skill`, `minExperience`, `maxExperience`, `minRate`, `maxRate`, `availability` (`AVAILABLE` or `LIMITED`), and `minRating` (0–5):

`GET /api/freelancers?skill=Java&minExperience=3&maxExperience=10&minRate=500&maxRate=1500&availability=AVAILABLE&minRating=4`

For a project-specific ranked list, a client calls `GET /api/projects/{projectId}/matches`. The score combines skill overlap (70%), experience fit (20%), and budget fit (10%), with each factor and its explanation returned in the response. Hourly projects compare the freelancer's hourly rate to the hourly budget range. For fixed-price projects, affordability uses an explicitly approximate 40-hour effort assumption to translate the total budget into an hourly cap.

## Submit a proposal

`POST /api/projects/{projectId}/proposals` (FREELANCER)

```json
{"coverLetter":"I have delivered similar booking systems.","proposedBudget":2400,"completionDays":28,"portfolioLinks":["https://example.com/work"]}
```

Clients review proposals at `GET /api/projects/{projectId}/proposals` and accept with `PUT /api/proposals/{id}/accept`. Acceptance creates the assignment.

## Milestones and payment status

Clients add milestones at `POST /api/assignments/{assignmentId}/milestones`. Freelancers submit work at `POST /api/milestones/{id}/submit`; clients approve with `PUT /api/milestones/{id}/approve` or request a revision with `PUT /api/milestones/{id}/request-revision`.

Checkout starts at `POST /api/payments/{id}/checkout`. A signed sandbox provider callback is posted to `POST /api/payments/webhook/sandbox`:

```json
{"transactionReference":"sandbox_example","successful":true,"signature":"<hex HMAC-SHA256 of sandbox_example|true>"}
```

The webhook signature uses `PAYMENT_SANDBOX_WEBHOOK_SECRET`. Never expose this secret in the frontend.

In the local `dev` profile, a client may complete a started sandbox payment without a provider by calling `POST /api/dev/payments/{paymentId}/simulate-success`. This development-only endpoint still requires the client's JWT and does not process real money. A paid sandbox payment can be downloaded as plain text from `GET /api/payments/{paymentId}/receipt` by either assignment participant.

## Messaging and notifications

Only participants in an accepted project assignment can open its conversation:

- `POST /api/projects/{projectId}/conversation` returns or creates the conversation.
- `GET /api/conversations/{conversationId}/messages?page=0&size=50` lists messages.
- `POST /api/conversations/{conversationId}/messages` with `{"body":"The first draft is ready."}` sends a message.
- `PUT /api/conversations/{conversationId}/read` marks messages from the other participant as read.
- `GET /api/notifications` and `GET /api/notifications/unread-count` return the signed-in user's notifications; `PUT /api/notifications/{id}/read` marks one as read.

## Files, reviews, and reports

Upload project attachments as multipart field `file` to `POST /api/projects/{projectId}/attachments` (client). Attachments for open projects can be listed and downloaded with the public project; after a project closes, access is limited to its client and assigned freelancer. Freelancers upload deliverables to `POST /api/submissions/{submissionId}/files` with the same field; submission files require authentication and assignment participation.

After an assignment is completed, either participant can create one review of the other at `POST /api/assignments/{assignmentId}/reviews`:

```json
{"rating":5,"feedback":"Clear communication and an excellent handoff."}
```

Member reports are submitted to `POST /api/reports` with exactly one target:

```json
{"targetUserId":42,"reason":"Please review this account."}
```

