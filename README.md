# CitiGrow.ai
Laying India’s Educational Foundation

## Problem Statement

Education is a fundamental pillar of human development. Yet for many learners in rural India, schooling does not consistently translate into foundational skills, practical capabilities or economic opportunity. Although school enrolment among children aged 6–14 reaches approximately 98%, high enrolment can coexist with substantial learning gaps.

These gaps persist into adolescence. Around one in four rural youth aged 14–18 cannot fluently read a second-grade-level text in their regional language, and more than 56% cannot solve a three-digit-by-one-digit division problem.

### Why does this gap persist?

Learning outcomes are shaped by several interconnected factors. CitiGrow focuses on helping address the challenges associated with teacher shortages, limited instructional support and administrative workloads.

- **Single-teacher and multigrade classrooms**: Many primary schools depend on a single teacher to teach multiple grades and subjects, limiting opportunities for individual attention.
  
- **Non-teaching responsibilities**: Administrative and other non-instructional duties can reduce the time teachers have available for teaching and student support.
  
- **Language and household barriers**: Learners may struggle to access understandable resources or receive informed educational and career guidance at home.

- **Career Awareness**: Majority of the students are not aware of the steps and do not have guidance to uplift themseleves.

## Our Solution

CitiGrow is built to transform rural learning from an isolated struggle into a connected ecosystem of opportunity. Our AI-enabled working prototype brings students, teachers and families together through regional-language audio guidance, personalized learning pathways and career exploration—helping bridge language barriers, extend teacher support and strengthen household participation. It represents an evolution from simply accessing education to building the skills, confidence and support needed to shape a better future, with the ambition to empower rural learners to uplift themselves, their families and their communities.

### What Makes CitiGrow Different?

- **Offline-first intelligence, built for rural realities**: Access to guidance should not depend on a reliable internet connection. CitiGrow generates supported speech directly on the device and caches it for reuse, rather than relying on prerecorded narration or cloud speech services. Our approach prioritizes local processing so that language support and essential guidance remain accessible in low-connectivity environments.

- **Household empowerment, not just individual learning**: A student’s journey is shaped by the people around them. CitiGrow involves families through dedicated parent and student experiences that explain learning pathways, potential costs and future opportunities. By helping households understand and support educational choices, we aim to strengthen students’ confidence, improve access to career guidance and help families break intergenerational cycles of disadvantage.
  
- **Start with the learner, not their certificate**: A learner’s class or qualification does not always reflect their current abilities. CitiGrow’s prototype includes foundational skills checks inspired by the principle of assessing actual learning levels rather than assuming readiness. These checks help identify support needs and guide learners toward achievable next steps—building confidence through progress without labelling or leaving anyone behind. They are not a validated ASER assessment.

### Current Technical Implementation

We have developed a working MVP to demonstrate the product’s core experience and establish its technical foundation.

#### Simple, Guided Interface

CitiGrow simplifies learning through clear screens, familiar navigation and one-step-at-a-time guidance. Regional-language audio and highlighted fields help users understand each action, while dropdowns reduce typing. A personalized Life Map turns ambitious goals into manageable next steps, and dedicated student, parent and teacher views keep information relevant—making the experience approachable for first-time smartphone users.
<p align="center">
<img width="200" alt="CitiGrow guided onboarding interface" src="https://github.com/user-attachments/assets/123ddb23-76d4-4c3e-9082-f9cac5f1b892" />
</p>

#### Local Text-to-Speech

CitiGrow reads onboarding instructions aloud in English, Hindi, Tamil and Malayalam, highlighting the relevant field as it speaks. Tamil and Malayalam use the bundled AI4Bharat VITS model, while English and Hindi use installed offline Android voices. Audio is generated on-device and cached for reuse, enabling spoken guidance without an internet connection once the required model or voice is available.

#### Access to Free Online Learning Resources

CitiGrow links learners to resources on SWAYAM, DIKSHA and Skill India, connecting learning opportunities with their career journey. The next phase aims to integrate supported course content within the app, reducing navigation between platforms and combining progress tracking with assessments to guide learners’ next steps.
<p align="center">
<img width="200" alt="CitiGrow learning resources" src="https://github.com/user-attachments/assets/32c5e0d2-25b7-4b6b-a7d1-ab24637ef80d" />
<img width="200" alt="CitiGrow course exploration" src="https://github.com/user-attachments/assets/3d926229-9219-49fb-b65e-37aaadcdff60" />
</p>

#### Personalization and Learner Profiles

CitiGrow builds a learner profile around interests, skills, available study time and practical constraints such as budget and location. Foundational skills checks help identify current strengths and learning needs. Rule-based recommendations then connect the profile to relevant career pathways and manageable learning steps, explaining why each option fits and which skills need strengthening.
<p align="center">
<img width="200" alt="CitiGrow learner assessment" src="https://github.com/user-attachments/assets/107464b5-c8d1-4ab7-9741-9b6f51f72253" />
<img width="200" alt="CitiGrow personalized learner profile" src="https://github.com/user-attachments/assets/83bef6da-e14b-44c0-bf02-7451ba952334" />
</p>

### Proposed Technical Improvements for the Next Phase

- **Integrated Course Access**: Reduce unnecessary redirection by integrating supported learning content through permitted embeds or provider partnerships. Where integration is unavailable, retain clear links to the original provider. Combine course participation with assessments rather than treating video watch time alone as evidence of learning.
  
- **AI-Powered Skills and Career Mapping**: Extend rule-based personalization with a structured map connecting careers, prerequisite skills, assessments and learning resources. Identify skill gaps and recommend achievable next steps, explaining why each resource is relevant. Recommendations will consider affordability, language, available time and location—not interests alone. We also propose a cloud-assisted service to identify emerging and non-traditional opportunities using verified sources, while keeping essential guidance available offline.

-** Voice-Based Interaction**: Build on the text-to-speech proof of concept by evaluating locally executed speech recognition, including Whisper-based implementations. This would allow learners to speak instead of typing and review the recognized text before submission. Language accuracy, response time, memory and battery consumption will need testing on target devices.

- **Offline-First Learning and Synchronization**: Enable access to permitted downloadable lessons, practice activities and progress records with limited connectivity. Store progress locally and synchronize it when connectivity returns, supporting shared phones and interrupted learning sessions.

- **Teacher-Assisted Learning and Intervention**: Expand the prototype’s local teacher views into a consent-based cohort dashboard showing progress, recurring difficulties and learners who may need assistance. Enable teachers to recommend resources and provide targeted support, extending their reach without positioning AI as a replacement for teachers.

- **Household Participation and Shared-Device Support**: Expand regional-language progress summaries and suggested learning schedules for families. Strengthen shared-device profiles and privacy controls so households can support learning without accessing a student’s private conversations.

- **Privacy, Security and Scalable Infrastructure**: Introduce production-ready authentication, organization-level access controls, transactional storage and reliable background synchronization. Collect only necessary information, make sharing explicit and establish appropriate safeguards for minors. These foundations will support responsible expansion across institutions and regions.

- **Measurable Learning and Livelihood Outcomes**: Evaluate progress through demonstrated skill improvement, course completion, eligible applications, training admissions and sustained livelihood progression. Pilot studies will also assess learner satisfaction, facilitator time saved and cost per verified outcome.

### Proposed Revenue Generation

- **Responsible Advertising**: Display relevant, clearly labelled advertisements to help cover platform costs, without using sensitive learner data for targeting and subject to appropriate safeguards for younger users.

- **Sponsored Course Discovery**: Offer verified providers paid visibility for courses and scholarship awareness campaigns, clearly separating sponsored content from personalized recommendations.

- **Premium Course Subscriptions**: Keep foundational guidance accessible while offering optional advanced courses through subscriptions, sharing revenue with educators and course providers.

- **Institutional Partnerships**: Offer schools, NGOs and training centres paid access to cohort management, teacher dashboards and implementation support. This model could support classroom adoption while expanding learners’ access to guidance and opportunities.

These are proposed revenue models; payment collection and commercial partnerships are not implemented in the current prototype.

### System Architecture

The architecture distinguishes the current on-device prototype from proposed cloud services and verified partner integrations.
<img width="2975" height="3943" alt="CitiGrow system architecture" src="https://github.com/user-attachments/assets/836fc233-77e3-4841-8089-dea9da214592" />

### New Student Journey

The student journey connects onboarding, discovery, skills checks and a personalized learning pathway with family and teacher support. Verified provider integrations and real-world outcome tracking are planned extensions.
<img width="1357" height="6174" alt="New student journey through the CitiGrow ecosystem" src="https://github.com/user-attachments/assets/7e99e604-4de7-45bf-ace8-7b9168511841" />
