package com.rowanmcalpin.nextftc.visualization

import org.rowlandhall.meepmeep.MeepMeep
import org.rowlandhall.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark
import org.rowlandhall.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark
import org.rowlandhall.meepmeep.roadrunner.DefaultBotBuilder
import org.rowlandhall.meepmeep.roadrunner.trajectorysequence.sequencesegment.SequenceSegment
import org.rowlandhall.meepmeep.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment
import org.rowlandhall.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import com.rowanmcalpin.nextftc.command.groups.CommandGroup
import com.rowanmcalpin.nextftc.Constants
import com.rowanmcalpin.nextftc.driving.DriveConstants
import com.rowanmcalpin.nextftc.driving.FollowTrajectory
import com.rowanmcalpin.nextftc.trajectories.TrajectoryFactory

object MeepMeepVisualizer {

    val robots = mutableListOf<MeepMeepRobot>()
    var mouseCoordinateDisplayPositionX: Int? = null
    var mouseCoordinateDisplayPositionY: Int? = null

    fun run(trajectoryFactory: TrajectoryFactory, windowSize: Int = 600, darkMode: Boolean = true, backgroundAlpha: Float = 0.95f, background: MeepMeep.Background = MeepMeep.Background.FIELD_INTOTHEDEEP_JUICE_DARK) {
        val meepMeep = MeepMeep(windowSize)
        meepMeep.setBackground(background)
            .setDarkMode(darkMode)
            .setBackgroundAlpha(backgroundAlpha)
        robots.forEach {
            Constants.drive = it.driver
            Constants.color = it.color
            trajectoryFactory.initialize()
            val constants: DriveConstants = it.driver.constants
            val botBuilder: DefaultBotBuilder = DefaultBotBuilder(meepMeep)
                .setDimensions(it.width, it.length)
                .setConstraints(
                    constants.MAX_VEL, constants.MAX_ACCEL,
                    constants.MAX_ANG_VEL, constants.MAX_ANG_ACCEL,
                    constants.TRACK_WIDTH
                ).setColorScheme(
                    if (it.color == Constants.Color.RED) ColorSchemeRedDark()
                    else ColorSchemeBlueDark()
                )
            meepMeep.addEntity(botBuilder.followTrajectorySequence(
                TrajectorySequence(
                    routineToSegmentList(it.routine.invoke())
                )
            ))
        }
        if(mouseCoordinateDisplayPositionX != null) {
            meepMeep.setMouseCoordinateDisplayPosition(mouseCoordinateDisplayPositionX!!,
                mouseCoordinateDisplayPositionY!!)
        }
        meepMeep.start()
    }

    fun addRobot(robot: MeepMeepRobot) {
        robots.add(robot)
    }

    private fun routineToSegmentList(routine: CommandGroup): List<SequenceSegment> {
        routine.onStart()
        val trajectories = arrayListOf<SequenceSegment>()
        for (command in routine.commands) {
            if (command is FollowTrajectory) {
                trajectories.add(TrajectorySegment(command.trajectory.trajectory))
            }
            if (command is CommandGroup) {
                trajectories.addAll(routineToSegmentList(command))
            }
        }
        return trajectories
    }

    fun setMouseCoordinateDisplayPosition(x: Int, y: Int) {
        mouseCoordinateDisplayPositionX = x
        mouseCoordinateDisplayPositionY = y
    }
}