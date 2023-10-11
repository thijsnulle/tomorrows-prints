package pipeline.steps

import pipeline.PipelineStep
import preview.Poster
import social.Influencer
import social.pinterest.PinterestInfluencer

class SocialMediaStep: PipelineStep {
    private val influencers: List<Influencer> = listOf(
        PinterestInfluencer(),
    )

    override fun process(posters: List<Poster>): List<Poster> {
        println("Posting ${posters.size} posters to Pinterest")

        posters.forEach { poster -> influencers.forEach {
            influencer -> influencer.post(poster)
        }}

        return posters
    }
}