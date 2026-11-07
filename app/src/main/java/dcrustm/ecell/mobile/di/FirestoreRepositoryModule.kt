package dcrustm.ecell.mobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dcrustm.ecell.mobile.data.repository.FirestoreAnnouncementRepository
import dcrustm.ecell.mobile.data.repository.FirestoreComplaintRepository
import dcrustm.ecell.mobile.data.repository.FirestoreEventRepository
import dcrustm.ecell.mobile.data.repository.FirestoreMemberRepository
import dcrustm.ecell.mobile.domain.repository.AnnouncementRepository
import dcrustm.ecell.mobile.domain.repository.ComplaintRepository
import dcrustm.ecell.mobile.domain.repository.EventRepository
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMemberRepository(impl: FirestoreMemberRepository): MemberRepository

    @Binds
    @Singleton
    abstract fun bindAnnouncementRepository(impl: FirestoreAnnouncementRepository): AnnouncementRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(impl: FirestoreComplaintRepository): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: FirestoreEventRepository): EventRepository

}
