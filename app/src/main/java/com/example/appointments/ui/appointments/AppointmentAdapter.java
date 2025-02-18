package com.example.appointments.ui.appointments;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appointments.R;
import com.example.appointments.model.Appointment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    // Keep appointments private
    public List<Appointment> appointments = new ArrayList<>();
    private Handler handler = new Handler();

    // Listener for item actions (delete and more details)
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onDelete(Appointment appointment, int position);
        void onMoreDetails(Appointment appointment, int position);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    // Public getter for appointments list
    public List<Appointment> getAppointments() {
        return appointments;
    }

    @Override
    public AppointmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppointmentAdapter.ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.titleTextView.setText(appointment.getTitle());
        holder.descriptionTextView.setText(appointment.getDescription());
        holder.timeTextView.setText(formatDateTime(appointment.getDateTimeMillis()));

        // Set hospital name if available
        if (appointment.getHospital() != null) {
            holder.hospitalTextView.setText(appointment.getHospital().getName());
        } else {
            holder.hospitalTextView.setText("No hospital selected");
        }

        // Cancel any previous countdown runnable for this holder
        if (holder.countdownRunnable != null) {
            holder.countdownTextView.removeCallbacks(holder.countdownRunnable);
        }
        // Create and start a new countdown Runnable updating every second
        holder.countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long millisUntil = appointment.getDateTimeMillis() - System.currentTimeMillis();
                holder.countdownTextView.setText(formatCountdown(millisUntil));
                // Post update every second if not expired
                if (millisUntil > 0) {
                    holder.countdownTextView.postDelayed(this, 1000);
                }
            }
        };
        holder.countdownTextView.post(holder.countdownRunnable);

        // Set delete button listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDelete(appointment, holder.getAdapterPosition());
                }
            }
        });

        // Set "More Details" button listener
        holder.moreDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onMoreDetails(appointment, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    // Helper method to format the date/time from milliseconds
    private String formatDateTime(long dateTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(dateTimeMillis));
    }

    // Helper method to format the countdown including seconds
    private String formatCountdown(long millisUntil) {
        if (millisUntil <= 0) {
            return "Expired";
        }
        long secondsTotal = millisUntil / 1000;
        long days = secondsTotal / 86400;
        long hours = (secondsTotal % 86400) / 3600;
        long minutes = (secondsTotal % 3600) / 60;
        long seconds = secondsTotal % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s remaining");
        return sb.toString();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView timeTextView;
        public TextView hospitalTextView;  // For hospital name
        public TextView countdownTextView;  // For countdown timer
        public ImageButton deleteButton;    // For deletion
        public ImageButton moreDetailsButton; // For "More Details"
        public Runnable countdownRunnable;  // Holds the current countdown Runnable

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            hospitalTextView = itemView.findViewById(R.id.textViewHospital);
            countdownTextView = itemView.findViewById(R.id.textViewCountdown);
            deleteButton = itemView.findViewById(R.id.btnDelete);
            moreDetailsButton = itemView.findViewById(R.id.btnMoreDetails);
        }
    }
}
