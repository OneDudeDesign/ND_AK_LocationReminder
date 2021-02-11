package com.udacity.project4.locationreminders.reminderdetails

import com.udacity.project4.base.BaseFragment
import org.koin.android.ext.android.inject

class ReminderDetailFragment : BaseFragment() {

    override val _viewModel: ReminderDetailViewModel by inject()
}