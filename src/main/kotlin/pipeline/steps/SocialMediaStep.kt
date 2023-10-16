package pipeline.steps

import io.github.oshai.kotlinlogging.KotlinLogging
import pipeline.PipelineStep
import preview.Poster
import social.Influencer
import social.pinterest.PinterestInfluencer
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class SocialMediaStep: PipelineStep() {

    private val logger = KotlinLogging.logger {}
    private val influencers: List<Influencer> = listOf(
        PinterestInfluencer(),
    )

    override fun process(posters: List<Poster>): List<Poster> {
        logger.info {
            "Posting [ ${posters.joinToString(", ") { it.path.nameWithoutExtension }} ] to Pinterest."
        }

        influencers.forEach { it.post(posters) }

        return posters
    }
}