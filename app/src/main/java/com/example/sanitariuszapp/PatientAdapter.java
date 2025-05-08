package com.example.sanitariuszapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.media.AudioAttributes;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sanitariuszapp.activities.MainActivity;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/***
 * Adapter for displaying a list of patients in a RecyclerView, handling item expansion and timers.
 ***/
public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    /***
     * List of patients to display and Android context.
     ***/
    private List<Patient> patientList;
    private Context context;
    private PatientDatabaseHelper dbHelper;

    /***
     * Index of the currently expanded item, -1 if none.
     ***/
    private int expandedPosition = -1;

    /***
     * Keeps track of which patients have already triggered a notification to avoid duplicates.
     ***/
    private static Set<Integer> notifiedPatientIds = new HashSet<>();

    /***
     * Constructor initializes context, data list, and database helper.
     ***/
    public PatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
        this.dbHelper = new PatientDatabaseHelper(context);
    }

    /***
     * Inflates the item layout and creates a new ViewHolder.
     ***/
    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient_test, parent, false);
        return new PatientViewHolder(view);
    }

    /***
     * Binds patient data to views, handles expansion, collapse, and procedure timers.
     ***/
    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        final Patient currentPatient = patientList.get(position);

        // Cancel any existing timer when rebinding
        if (holder.activeTimer != null) {
            holder.activeTimer.cancel();
            holder.activeTimer = null;
        }

        // Reset view transforms
        holder.foregroundLayout.setScaleX(1.0f);
        holder.foregroundLayout.setScaleY(1.0f);
        holder.foregroundLayout.setTranslationX(0);

        // Set room number and patient name
        holder.tvRoomNumber.setText(currentPatient.getRoomNumber());
        holder.tvPatientName.setText(currentPatient.getName());

        // Reset status background and hide timer by default
        Drawable defaultBg = holder.patientStatus.getBackground();
        if (defaultBg != null) {
            Drawable bgWrap = DrawableCompat.wrap(defaultBg.mutate());
            holder.patientStatus.setBackground(bgWrap);
            DrawableCompat.setTint(bgWrap, Color.parseColor("#80CFA9"));
        }
        holder.tvProcedureTimer.setVisibility(View.GONE);
        holder.tvProcedureTimer.setText("");

        // Setup timer and color animation for any IN_PROGRESS procedure
        setupInProgressOnItem(holder, currentPatient);

        // Show expanded or collapsed view based on position
        if (position == expandedPosition) {
            showExpandedView(holder, currentPatient);
        } else {
            showCollapsedView(holder, currentPatient);
        }

        // Handle click to expand or collapse
        holder.foregroundLayout.setOnClickListener(v -> {
            int oldPos = expandedPosition;
            int currentPos = holder.getAdapterPosition();
            if (oldPos == currentPos) {
                expandedPosition = -1;
                notifyItemChanged(oldPos);
            } else {
                expandedPosition = currentPos;
                if (oldPos != -1) notifyItemChanged(oldPos);
                notifyItemChanged(currentPos);
            }
        });

        // Handle long click to open patient details
        holder.foregroundLayout.setOnLongClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailsActivity.class);
            intent.putExtra("patient_id", currentPatient.getId());
            context.startActivity(intent);
            return true;
        });
    }

    /***
     * Returns the total number of patients in the list.
     ***/
    @Override
    public int getItemCount() {
        return patientList.size();
    }

    /***
     * Indicates if any item is currently expanded.
     ***/
    public boolean isItemExpanded() {
        return expandedPosition != -1;
    }

    /***
     * Collapse any expanded item.
     ***/
    public void collapseExpandedItem() {
        if (expandedPosition != -1) {
            int old = expandedPosition;
            expandedPosition = -1;
            notifyItemChanged(old);
        }
    }

    /***
     * Shows detailed view including notes and full procedure list in a column layout.
     ***/
    private void showExpandedView(PatientViewHolder holder, Patient patient) {
        if (patient.getNote() != null && !patient.getNote().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(patient.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        FlexboxLayout flexLayout = holder.llProcedures;
        flexLayout.removeAllViews();
        flexLayout.setFlexWrap(FlexWrap.NOWRAP);
        flexLayout.setFlexDirection(FlexDirection.COLUMN);

        int desiredWidth = context.getResources().getDisplayMetrics().widthPixels / 2;

        // Sort procedures: in-progress first, then normal, then finished
        List<Procedure> inProgress = new ArrayList<>();
        List<Procedure> normal = new ArrayList<>();
        List<Procedure> finished = new ArrayList<>();
        for (Procedure proc : patient.getProcedures()) {
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) inProgress.add(proc);
            else if (proc.getStatus() == Procedure.STATUS_FINISHED) finished.add(proc);
            else normal.add(proc);
        }
        List<Procedure> sortedProcedures = new ArrayList<>();
        sortedProcedures.addAll(inProgress);
        sortedProcedures.addAll(normal);
        sortedProcedures.addAll(finished);

        // Display each procedure with appropriate background and time
        for (Procedure proc : sortedProcedures) {
            TextView tvProc = new TextView(context);
            tvProc.setTextSize(16);
            tvProc.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            llParams.setMargins(5, 5, 5, 5);
            tvProc.setLayoutParams(llParams);
            String time = (proc.getTime() == null || proc.getTime().isEmpty()) ? "No time" : proc.getTime();
            tvProc.setText(proc.getText() + " (" + time + ")");
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) {
                tvProc.setBackgroundResource(R.drawable.procedure_in_progress_drawable);
            } else if (proc.getStatus() == Procedure.STATUS_FINISHED) {
                tvProc.setBackgroundResource(R.drawable.procedure_finished_drawable);
            } else if (proc.getStatus() == Procedure.STATUS_MARKED) {
                tvProc.setBackgroundResource(R.drawable.procedure_marked_drawable);
            } else {
                tvProc.setBackgroundResource(R.drawable.procedure_default_drawable);
            }
            tvProc.setTextColor(ContextCompat.getColor(context, R.color.Text));
            flexLayout.addView(tvProc);
        }
    }

    /***
     * Shows a compact view of procedures in a row layout.
     ***/
    private void showCollapsedView(PatientViewHolder holder, Patient patient) {
        holder.tvNote.setVisibility(View.GONE);
        FlexboxLayout flexLayout = holder.llProcedures;
        flexLayout.removeAllViews();
        flexLayout.setFlexWrap(FlexWrap.WRAP);
        flexLayout.setFlexDirection(FlexDirection.ROW);

        // Sort and display procedures similarly to expanded view
        List<Procedure> inProgress = new ArrayList<>();
        List<Procedure> normal = new ArrayList<>();
        List<Procedure> finished = new ArrayList<>();
        for (Procedure proc : patient.getProcedures()) {
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) inProgress.add(proc);
            else if (proc.getStatus() == Procedure.STATUS_FINISHED) finished.add(proc);
            else normal.add(proc);
        }
        List<Procedure> sortedProcedures = new ArrayList<>();
        sortedProcedures.addAll(inProgress);
        sortedProcedures.addAll(normal);
        sortedProcedures.addAll(finished);

        for (Procedure proc : sortedProcedures) {
            TextView tvProc = new TextView(context);
            tvProc.setTextSize(16);
            tvProc.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(5, 5, 5, 5);
            tvProc.setLayoutParams(lp);
            tvProc.setText(proc.getText());
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) {
                tvProc.setBackgroundResource(R.drawable.procedure_in_progress_drawable);
            } else if (proc.getStatus() == Procedure.STATUS_FINISHED) {
                tvProc.setBackgroundResource(R.drawable.procedure_finished_drawable);
            } else if (proc.getStatus() == Procedure.STATUS_MARKED) {
                tvProc.setBackgroundResource(R.drawable.procedure_marked_drawable);
            } else {
                tvProc.setBackgroundResource(R.drawable.procedure_default_drawable);
            }
            tvProc.setTextColor(ContextCompat.getColor(context, R.color.Text));
            flexLayout.addView(tvProc);
        }
    }

    /***
     * Checks for an in-progress procedure, updates timer display and background color accordingly.
     ***/
    private void setupInProgressOnItem(PatientViewHolder holder, Patient patient) {
        Procedure inProgressProc = null;
        for (Procedure proc : patient.getProcedures()) {
            if (proc.getStatus() == Procedure.STATUS_IN_PROGRESS) {
                inProgressProc = proc;
                break;
            }
        }
        if (inProgressProc == null) {
            // No in-progress procedure: hide timer and reset background
            holder.tvProcedureTimer.setVisibility(View.GONE);
            Drawable defaultBg = holder.patientStatus.getBackground();
            if (defaultBg != null) DrawableCompat.setTint(defaultBg, Color.parseColor("#80CFA9"));
            holder.currentFinishTime = 0;
            return;
        }
        final Patient finalPatient = patient;
        final Procedure inProgressFinal = inProgressProc;

        // Show timer and calculate finish time
        holder.tvProcedureTimer.setVisibility(View.VISIBLE);
        final long finishTime = inProgressProc.getStartTimestamp() + inProgressProc.getDurationMillis();
        if (holder.currentFinishTime == finishTime && holder.activeTimer != null) return;
        if (holder.activeTimer != null) holder.activeTimer.cancel();
        holder.currentFinishTime = finishTime;

        long remaining = finishTime - System.currentTimeMillis();
        Drawable originalBg = holder.patientStatus.getBackground();
        final Drawable wrappedBg = (originalBg != null) ? DrawableCompat.wrap(originalBg.mutate()) : null;
        int initialColor = getDesiredColor(inProgressProc.getDurationMillis(), remaining);
        if (wrappedBg != null) {
            DrawableCompat.setTint(wrappedBg, initialColor);
            holder.patientStatus.setBackground(wrappedBg);
        }

        if (remaining > 0) {
            holder.tvProcedureTimer.setText(formatTime(remaining));
            holder.activeTimer = new CountDownTimer(remaining, 1000) {
                int prevColor = initialColor;
                @Override public void onTick(long millisUntilFinished) {
                    holder.tvProcedureTimer.setText(formatTime(millisUntilFinished));
                    int newColor = getDesiredColor(inProgressFinal.getDurationMillis(), millisUntilFinished);
                    if (newColor != prevColor && wrappedBg != null) {
                        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), prevColor, newColor);
                        colorAnim.setDuration(800);
                        colorAnim.addUpdateListener(anim -> DrawableCompat.setTint(wrappedBg, (int) anim.getAnimatedValue()));
                        colorAnim.start();
                        prevColor = newColor;
                    }
                }
                @Override public void onFinish() {
                    holder.tvProcedureTimer.setText("00:00");
                    if (wrappedBg != null) {
                        ValueAnimator endAnim = ValueAnimator.ofObject(new ArgbEvaluator(), prevColor, Color.RED);
                        endAnim.setDuration(800);
                        endAnim.addUpdateListener(anim -> DrawableCompat.setTint(wrappedBg, (int) anim.getAnimatedValue()));
                        endAnim.start();
                    }
                    if (!notifiedPatientIds.contains(finalPatient.getId())) {
                        showNotification(finalPatient);
                        notifiedPatientIds.add(finalPatient.getId());
                    }
                    startCountUpTimer(holder, finishTime);
                }
            };
            holder.activeTimer.start();
        } else {
            // Procedure already finished: show negative timer and send notification if needed
            holder.tvProcedureTimer.setText("-" + formatTime(System.currentTimeMillis() - finishTime));
            if (wrappedBg != null) DrawableCompat.setTint(wrappedBg, Color.RED);
            if (!notifiedPatientIds.contains(patient.getId())) {
                showNotification(patient);
                notifiedPatientIds.add(patient.getId());
            }
            startCountUpTimer(holder, finishTime);
        }
    }

    /***
     * Starts a count-up timer showing how long it's been since the procedure finished.
     ***/
    private void startCountUpTimer(PatientViewHolder holder, long finishTime) {
        CountDownTimer countUp = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override public void onTick(long l) {
                long diff = System.currentTimeMillis() - finishTime;
                holder.tvProcedureTimer.setText("-" + formatTime(diff));
            }
            @Override public void onFinish() {}
        };
        holder.activeTimer = countUp;
        holder.tvProcedureTimer.post(countUp::start);
    }

    /***
     * Formats milliseconds into MM:SS string.
     ***/
    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / 60000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    /***
     * Determines the background color based on remaining time ratio.
     ***/
    private int getDesiredColor(long duration, long remaining) {
        float ratio = duration > 0 ? (float) remaining / duration : 0;
        if (ratio >= 0.67f) return Color.parseColor("#00c6bc");
        else if (ratio >= 0.33f) return Color.parseColor("#F7D060");
        else return Color.parseColor("#D1545B");
    }

    /***
     * ViewHolder class holds references to item views.
     ***/
    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView foregroundLayout;
        TextView tvRoomNumber, tvPatientName, tvNote;
        FlexboxLayout llProcedures;
        View patientStatus;
        TextView tvProcedureTimer;
        CountDownTimer activeTimer = null;
        long currentFinishTime = 0;

        /***
         * ViewHolder constructor retrieves view references.
         ***/
        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            foregroundLayout = itemView.findViewById(R.id.foreground_layout);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvNote = itemView.findViewById(R.id.tvNote);
            llProcedures = itemView.findViewById(R.id.llProcedures);
            patientStatus = itemView.findViewById(R.id.PatientStatus);
            tvProcedureTimer = itemView.findViewById(R.id.tvProcedureTimer);
        }
    }

    /***
     * Creates and displays a notification when a procedure is ready for pickup.
     ***/
    private void showNotification(Patient patient) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "custom_channel")
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("Procedure Ready")
                .setContentText("Patient " + patient.getName() + " in room " + patient.getRoomNumber() + " ready to pick up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_sound);
        builder.setSound(soundUri)
                .setVibrate(new long[]{0,500,250,500,1000,543,4324,400})
                .setLights(Color.BLUE,1000,500);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "custom_channel";
            CharSequence channelName = "Custom Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            if (notificationManager != null) {
                NotificationChannel existing = notificationManager.getNotificationChannel(channelId);
                if (existing != null) notificationManager.deleteNotificationChannel(channelId);
            }
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, attrs);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
        if (notificationManager != null) notificationManager.notify(patient.getId(), builder.build());
    }
}
